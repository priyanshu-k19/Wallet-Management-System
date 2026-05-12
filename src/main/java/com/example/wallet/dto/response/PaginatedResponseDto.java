package com.example.wallet.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaginatedResponseDto<T> {

    private List<T> content;
    private int pageNumber;
    private int totalPages;
}
