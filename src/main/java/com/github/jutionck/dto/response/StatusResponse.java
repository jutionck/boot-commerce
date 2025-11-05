package com.github.jutionck.dto.response;


import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusResponse {
    private Integer code;
    private String description;
}
