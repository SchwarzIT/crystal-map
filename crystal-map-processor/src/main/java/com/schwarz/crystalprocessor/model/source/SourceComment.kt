package com.schwarz.crystalprocessor.model.source

import com.schwarz.crystalapi.Comment
import com.schwarz.crystalcore.model.source.ISourceComment

class SourceComment(commentAnnotation: Comment) : ISourceComment {
    override val comment: Array<String> = commentAnnotation.comment
}