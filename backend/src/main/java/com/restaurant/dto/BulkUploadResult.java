package com.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkUploadResult {
    private int created;
    private int failed;
    private List<String> errors = new ArrayList<>();
}
