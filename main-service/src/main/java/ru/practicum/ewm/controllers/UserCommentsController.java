package ru.practicum.ewm.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.CommentDto;
import ru.practicum.ewm.serices.CommentService;


@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/users/{userId}/comments")
public class UserCommentsController {
    private final CommentService commentService;

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long userId,
                       @PathVariable long commentId) {
        commentService.deleteComment(commentId, userId);
    }

    @PatchMapping("/{commentId}")
    public CommentDto update(@PathVariable long userId,
                             @PathVariable long commentId,
                             @RequestBody @Valid CommentDto commentDto) {
        return commentService.updateComment(commentId, userId, commentDto);
    }
}
