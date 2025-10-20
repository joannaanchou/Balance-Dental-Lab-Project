package com.example.demo.dto;

import lombok.Data;

@Data
public class CaseProductLine {
    private Long categoryId;    // 前端選到的 category.id
    private Long itemId;        // 前端選到的 item.id
    private Integer quantity;   // 數量
    private Integer toothCode;  // 齒位(用整數)，例如 11、12...
}
