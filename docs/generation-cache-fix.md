# Generation Cache: Bug Analysis and Fix

This document describes why the persistent `GenerationCache` (introduced with
"Support full incremental build") broke incremental builds, why it could not be
repaired in place, and how its goals are now achieved safely.

Related commits:

| Commit | Change |
|---|---|
| `1d83668` | Remove `GenerationCache` from KSP-managed outputs, add content-based skip for doc/schema side outputs |
| `441d4dd` | Merge doc/schema outputs across incremental processing runs |
| `5e2cf96` | Clear `ProcessingContext` state on every `finish()` exit path |
| `20fa546` | Code-review fixes: source-file-based purge, `onError()` cleanup, `.model` sidecars, deprecated cache options, atomic writes |

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
already contains exactly the new content. Writes go through a temp file plus
atomic rename, so concurrent readers never observe a torn write. This keeps
timestamps stable for consumers watching those files.

### 3. Side outputs: merge and purge across incremental runs

Incremental KSP runs only hand the processor the changed symbols. The aggregate
side outputs used to be overwritten with just that subset — a schema of 21
entities shrank to 1 after touching a single source file. The generators now
persist a model of their last run and merge:

- **`SchemaGenerator`** — the schema JSON keeps its published format
  (`List<EntitySchema>`) and is its own model; the per-entity source file paths
  live in a sidecar (`<fileName>.model`, JSON content). The sidecar deliberately
  does not use a `.json` extension because the versioning plugin parses every
  `*.json` file in its schema directories.
- **`DocumentationGenerator` / `EntityRelationshipGenerator`** — HTML and DOT
  cannot be parsed back losslessly, so each persists its segment model
  (rendered segment plus source file paths) in the same kind of `.model`
  sidecar next to the output file.

Merging is source-file based (`com.schwarz.crystalcore.util.SideOutputMerge`)
and needs no full-vs-incremental detection (KSP2's `Resolver.getAllFiles()` is
scoped to the dirty files of the round, so run types cannot be told apart). A
previously generated entry survives a run unless there is positive evidence
that it is gone:

1. the current run produced the entry again — current wins,
2. all of its recorded source files were deleted — purged,
3. one of its source files was reprocessed in this run without producing the
   entry again (annotation removed, entity renamed) — purged.

This purges deleted and renamed entities on *incremental* builds, not just on
full rebuilds. Entries without recorded sources (kapt, pre-existing files) are
kept conservatively.

**Migration**: an output file written by an older version has no `.model`
sidecar. A partial run cannot rebuild the full document, so the generators keep
the complete-but-stale file frozen until a run covers at least as many entities
as the document lists (a full rebuild) — that run rebuilds the file and
restores the sidecar. The schema JSON is not affected (it is its own model).

### 4. Daemon-safe processor state

`CrystalProcessor` clears its static `ProcessingContext` state on every exit
path: `finish()` uses try/finally, and `onError()` — which KSP calls *instead
of* `finish()` when errors were reported — is overridden to clean up as well.
Without this, stale registries survive in the Gradle daemon and cause spurious
"Duplicate Entity class found" errors after a failed build.

### 5. Deprecated options instead of silent removal

`CrystalProcessorProvider` retains the `CACHE_DIR_OPTION_NAME`,
`CACHE_ENABLED_OPTION_NAME` and `CACHE_FILE_NAME` constants as `@Deprecated`
for compatibility, and passing `crystal.cache.dir` or
`crystal.incremental.cache` produces a KSP warning explaining that the cache
was removed.

## Known limitations

- Entries recorded without source paths (written by kapt or by a pre-sidecar
  version) cannot be purged automatically until they are reprocessed once;
  a clean build always produces exact outputs.
- All build variants of a module write the side outputs to the same configured
  path (single `ksp {}` block). Entities that exist only in one variant's
  source set therefore accumulate in the shared file; per-variant output paths
  would require variant-specific KSP arguments.

## Verification

- Unit tests: `FileUtilTest`, `SideOutputMergeTest` (`crystal-map-core`).
- End-to-end on the demo module: full build produces 21 entities in
  `demo_schema.json`; an incremental build after touching one source file keeps
  all 21 (previously 1); a repeated no-op run leaves file timestamps untouched;
  adding an entity yields 22 while preserving the rest; deleting that entity
  purges it again on the following *incremental* build (21 — previously it
  lingered until a clean build); documentation HTML behaves identically. With
  the sidecar removed, an incremental run keeps the document frozen and a
  covering rebuild restores it.
