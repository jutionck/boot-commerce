package com.github.jutionck.dto.response;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagingResponse {
    private Integer page;
    private Integer rowsPerPage;
    private Long totalRows;
    private Integer totalPages;
    private Boolean hasNext;
    private Boolean hasPrevious;
}
