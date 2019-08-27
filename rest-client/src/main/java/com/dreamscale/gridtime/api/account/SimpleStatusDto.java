package com.dreamscale.gridtime.api.account;

import com.dreamscale.gridtime.api.status.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SimpleStatusDto {
    Status status;
    String message;
}
