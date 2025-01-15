package ru.practicum.ewm.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.CommentDto;
import ru.practicum.ewm.serices.CommentService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/events/{eventId}/comments")
public class EventCommentsController {
    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto create(@PathVariable long eventId,
                             @RequestParam long userId,
                             @RequestBody @Valid CommentDto commentDto) {
        return commentService.createComment(eventId, userId, commentDto);
    }

    @GetMapping
    public List<CommentDto> getAll(@PathVariable long eventId,
                                   @RequestParam(defaultValue = "0") int from,
                                   @RequestParam(defaultValue = "10") int size) {
        return commentService.getAll(eventId, from, size);
    }
}
