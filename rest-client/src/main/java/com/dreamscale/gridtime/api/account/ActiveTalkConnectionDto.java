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
public class ActiveTalkConnectionDto {

    private UUID connectionId;
    private String userName;
    Status status;
    String message;

    private List<ActiveRoomDto> activeRooms;

    public void addRoom(UUID roomId, String roomName) {
        if (activeRooms == null) {
            activeRooms = new ArrayList<>();
        }

        activeRooms.add(new ActiveRoomDto(roomId, roomName));
    }
}
