package com.goalracha.client.board.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.goalracha.client.board.domain.Board;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardDTO {

    private Long bno; //게시판 일련번호
    private String title; //제목
    private String content; //내용

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") // 년월일
    private Date createdate; //작성일시
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") // 년월일
    private Date editdate; //수정일시


    private Long state; //게시판 상태
    private Long uno; //유저 일련번호

    @JsonIgnore
    public Date getCreatedate() {
        return createdate;
    }


}
