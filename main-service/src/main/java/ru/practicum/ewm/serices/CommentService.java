package ru.practicum.ewm.serices;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.CommentDto;
import ru.practicum.ewm.exceptions.ConflictException;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.repository.CommentRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.UserRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Transactional
    public void deleteComment(long commentId) {
        if (!commentRepository.existsById(commentId))
            throw new NotFoundException("Not found comment", commentId);

        commentRepository.deleteById(commentId);
    }

    @Transactional
    public void deleteComment(long commentId, long userId) {
        var comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Not found comment", commentId));

        if (comment.getAuthor().getId() != userId)
            throw new ConflictException("User is not the author of comment.");

        commentRepository.deleteById(commentId);
    }

    @Transactional
    public CommentDto createComment(long eventId, long userId, CommentDto commentDto) {
        var userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user", userId));

        var eventEntity = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Not found event", eventId));

        var commentEntity = Mapper.toCommentEntity(commentDto);

        commentEntity.setCreated(Timestamp.valueOf(LocalDateTime.now()));
        commentEntity.setAuthor(userEntity);
        commentEntity.setEventEntity(eventEntity);

        commentRepository.saveAndFlush(commentEntity);

        return Mapper.toCommentDto(commentEntity);
    }

    @Transactional
    public CommentDto updateComment(long commentId, long userId, CommentDto commentDto) {
        var text = commentDto.text();

        if (text == null || text.isBlank())
            return commentDto;

        var commentEntity = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Not found comment", commentId));

        if (commentEntity.getAuthor().getId() != userId)
            throw new ConflictException("User is not the author of comment.");

        commentEntity.setText(text);
        commentEntity.setLastUpdateTime(Timestamp.valueOf(LocalDateTime.now()));

        commentRepository.saveAndFlush(commentEntity);

        return Mapper.toCommentDto(commentEntity);
    }

    public List<CommentDto> getAll(long eventId, int from, int size) {
        var pageable = initPageable(from, size);

        var page = commentRepository.findByEventEntityId(eventId, pageable);

        return page.stream()
                .map(Mapper::toCommentDto)
                .toList();
    }

    public static Pageable initPageable(int from, int size) {
        if (from <= 0)
            return PageRequest.ofSize(size);

        return PageRequest.of(from / size, size);
    }
}
