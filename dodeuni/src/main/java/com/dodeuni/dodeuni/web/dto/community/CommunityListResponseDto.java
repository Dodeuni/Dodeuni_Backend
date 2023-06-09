package com.dodeuni.dodeuni.web.dto.community;

import com.dodeuni.dodeuni.domain.community.Community;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommunityListResponseDto {

    private Long id;

    private LocalDateTime createdDateTime;

    private Long userId;

    private String email;

    private String nickname;

    private String main;

    private String sub;

    private String title;

    private String content;

    private Long thumbnailId;

    private String thumbnailUrl;

    public CommunityListResponseDto(Community community) {
        this.id = community.getId();
        this.createdDateTime = community.getCreatedDateTime();
        this.userId = community.getUserId().getId();
        this.email = community.getUserId().getEmail();
        this.nickname = community.getUserId().getNickname();
        this.main = community.getMain();
        this.sub = community.getSub();
        this.title = community.getTitle();
        this.content = community.getContent();
        if (!community.getPhotoList().isEmpty()) {
            this.thumbnailId = community.getPhotoList().get(0).getId();
            this.thumbnailUrl = community.getPhotoList().get(0).getPhotoUrl();
        }
        else {
            this.thumbnailId = null;
            this.thumbnailUrl = null;
        }
    }
}
