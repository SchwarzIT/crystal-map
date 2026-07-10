# Generation Cache: Bug Analysis and Fix

This document describes why the persistent `GenerationCache` (introduced with
"Support full incremental build") broke incremental builds, why it could not be
repaired in place, and how its goals are now achieved safely.

Related commits:

| Commit | Change |
|---|---|
| `1d83668` | Remove `GenerationCache` from KSP-managed outputs, add content-based skip for doc/schema side outputs |
| `441d4dd` | Merge doc/schema outputs across incremental processing runs |

## Background

The `GenerationCache` persisted a `packageName/fileName → SHA-256(content)` map
in a TSV file (`crystal.cache.dir` KSP option, e.g.
`build/generated/ksp/crystal-cache/crystal-map-cache.tsv`). Before writing a
generated Kotlin file through KSP's `CodeGenerator`, `KSPCodeGenerator` skipped
`createNewFile` when the content hash was unchanged since the previous run. The
intent was to avoid rewriting unchanged files on every build.

## The bug

KSP owns the lifecycle of generated files. Its contract per processing run:

1. **Source file unchanged** — its symbols are *not* handed to the processor;
   KSP keeps the previously generated outputs.
2. **Source file dirty** — KSP deletes its outputs before the run and expects
   the processor to re-register them via `createNewFile`.
3. **Full reprocess** (new source file, classpath change, task rerun) — all
   outputs are discarded and every file must be re-registered.

There is no case in which the processor is invoked *and* the old output still
exists. Skipping `createNewFile` therefore always means: the file ends up
missing. Because the cache TSV lived *outside* the KSP output directory, it
survived exactly the situations in which the outputs did not:

| Scenario (cache enabled) | Result |
|---|---|
| Modify an existing source file | ❌ all generated classes gone, `Unresolved reference` |
| Add a new entity source file | ❌ same |
| Dependency/AGP/Kotlin version bump | ❌ same |
| `--rerun-tasks` | ❌ same |
| Clean build | ✅ (cache file deleted together with `build/`) |

Reproduction on the old code: enable `crystal.cache.dir`, run a full build,
append a comment to any source file of the module, build again.

## Why the cache could not be fixed in place

The only safe skip condition would be "the target file still exists with
identical content". The processor cannot check this: the `CodeGenerator` API
deliberately hides output locations, and a shared cache directory is consulted
by every build variant (debug/release) while their output directories differ.
Every correct variant of the idea degenerates to "always write" — KSP's own
incrementality already *is* this cache, implemented at the layer that owns the
output lifecycle (case 1 above never reaches the processor at all). Downstream
cost of rewriting an identical file is also ~zero, since Gradle's up-to-date
checks are content-based.

## The fix

### 1. KSP-managed Kotlin outputs: always write (`1d83668`)

`GenerationCache` and the `crystal.cache.dir` / `crystal.incremental.cache`
options were removed. `KSPCodeGenerator` now always registers its files.
Passing the removed options is harmless; unknown KSP options are ignored.

### 2. Side outputs: content-based skip (`1d83668`)

The legitimate part of the idea — not rewriting unchanged files — moved to the
outputs that KSP does *not* manage and therefore never deletes: documentation
HTML, entity-relationship graph and schema JSON. These are written via
`File.writeTextIfChanged(...)` (`crystal-map-core`,
`com.schwarz.crystalcore.util.FileUtil`), which skips the write when the file
already contains exactly the new content. This keeps timestamps stable for
consumers watching those files.

### 3. Side outputs: merge across incremental runs (`441d4dd`)

Incremental KSP runs only hand the processor the changed symbols. The aggregate
side outputs used to be overwritten with just that subset — a schema of 21
entities shrank to 1 after touching a single source file. The generators now
merge with the previously written file (current run wins per entity) and render
deterministically sorted by entity name:

- **`SchemaGenerator`** — the JSON file is its own model: parse, merge by
  `EntitySchema.name`, write.
- **`DocumentationGenerator` / `EntityRelationshipGenerator`** — HTML and DOT
  cannot be parsed back losslessly, so both embed their segment model as a
  Base64-encoded JSON comment in the generated file and load it back on the
  next run:

  ```
  <!--crystal-map-model:eyJQcm9kdWN0IjoiPGRpdiBpZD0i…-->   (HTML)
  // crystal-map-model:eyJub2RlcyI6eyJQcm9kdWN0Ijo…        (DOT)
  ```

  Encoding/decoding lives in `com.schwarz.crystalcore.util.EmbeddedModel`.
  A missing or corrupt model comment falls back to the previous behavior
  (file is rebuilt from the current run only).

## Known limitation

Entities deleted from source stick around in documentation and schema until the
next clean/full generation — a removal cannot be detected from a partial model.
If exact outputs are required (e.g. for schema versioning releases), generate
them from a clean build.

## Verification

- Unit tests: `FileUtilTest`, `EmbeddedModelTest`, `SchemaGeneratorTest`
  (`crystal-map-core`).
- End-to-end on the demo module: full build produces 21 entities in
  `demo_schema.json`; an incremental build after touching one source file keeps
  all 21 (previously 1); a repeated no-op run leaves file timestamps untouched;
  adding an entity yields 22 while preserving the rest; same behavior for the
  documentation HTML.
