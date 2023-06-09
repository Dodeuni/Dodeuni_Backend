package com.dodeuni.dodeuni.service;

import com.dodeuni.dodeuni.domain.comment.Comment;
import com.dodeuni.dodeuni.domain.comment.CommentRepository;
import com.dodeuni.dodeuni.domain.community.Community;
import com.dodeuni.dodeuni.domain.community.CommunityRepository;
import com.dodeuni.dodeuni.domain.user.User;
import com.dodeuni.dodeuni.domain.user.UserRepository;
import com.dodeuni.dodeuni.web.dto.comment.CommentResponseDto;
import com.dodeuni.dodeuni.web.dto.comment.CommentSaveRequestDto;
import com.dodeuni.dodeuni.web.dto.comment.CommentUpdateRequestDto;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommentService {
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final CommentRepository commentRepository;
    private final FcmService fcmService;

    /* 댓글 등록 */
    @Transactional
    public List<CommentResponseDto> saveComment(CommentSaveRequestDto requestDto) {
        Community community = communityRepository.findById(requestDto.getCid())
                .orElseThrow(IllegalArgumentException::new);

        User user = userRepository.findById(requestDto.getUid())
                .orElseThrow(IllegalArgumentException::new);

        Comment comment = requestDto.toEntity();
        comment.setCommunity(community);
        comment.setUser(user);
        commentRepository.save(comment);

        // 게시글 댓글 알림
        try {
            sendAlarm(community, user);
        } catch (FirebaseMessagingException e) {
            log.info(e.getMessage());
        } finally {

            return commentRepository.findAllByCommunityOrderByCreatedDateTimeAsc(community)
                    .stream()
                    .map(CommentResponseDto::new)
                    .collect(Collectors.toList());
        }
    }

    /* 댓글 리스트 조회 */
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getCommentList(Long cid){
        Community community = communityRepository.findById(cid)
                .orElseThrow(IllegalArgumentException::new);
        return commentRepository.findByCommunity(community)
                .stream()
                .map(CommentResponseDto::new)
                .collect(Collectors.toList());
    }

    /* 댓글 수정 */
    @Transactional
    public List<CommentResponseDto> updateComment(CommentUpdateRequestDto requestDto){
        Comment comment = commentRepository.findById(requestDto.getId())
                .orElseThrow(IllegalArgumentException::new);

        comment.updateComment(requestDto.getContent());

        Community community = communityRepository.findById(requestDto.getCid())
                .orElseThrow(IllegalArgumentException::new);

        return commentRepository.findByCommunity(community)
                .stream()
                .map(CommentResponseDto::new)
                .collect(Collectors.toList());
    }

    /* 댓글 삭제 */
    @Transactional
    public List<CommentResponseDto> deleteComment(Long id, Long cid){
        Comment comment = commentRepository.findById(id)
                .orElseThrow(IllegalArgumentException::new);
        commentRepository.delete(comment);

        Community community = communityRepository.findById(cid)
                .orElseThrow(IllegalArgumentException::new);

        return commentRepository.findByCommunity(community)
                .stream()
                .map(CommentResponseDto::new)
                .collect(Collectors.toList());
    }

    /* 게시글 댓글 알림 */
    @Transactional
    public void sendAlarm(Community community, User user) throws FirebaseMessagingException {

        fcmService.sendMessage(community.getUserId().getFcmToken(), user.getNickname()+"님이 회원님의 게시글에 댓글을 달았습니다.", community.getId());
        System.out.println("게시글 작성자: "+community.getUserId().getNickname());
        System.out.println(user.getNickname()+"님이 회원님의 게시글에 댓글을 달았습니다.");
    }
}
