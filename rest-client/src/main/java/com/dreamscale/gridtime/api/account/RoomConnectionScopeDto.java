package com.dreamscale.gridtime.api.account;

import com.dreamscale.gridtime.api.status.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomConnectionScopeDto {

    private UUID connectionId;

    private List<UUID> roomIdsToJoin;

    public void addRoomId(UUID roomId) {
        if (roomIdsToJoin == null) {
            roomIdsToJoin = new ArrayList<>();
        }

        roomIdsToJoin.add(roomId);
    }
}
