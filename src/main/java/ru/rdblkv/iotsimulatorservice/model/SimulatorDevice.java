package ru.rdblkv.iotsimulatorservice.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("simulator_devices")
public class SimulatorDevice implements Persistable<String> {

    @Id
    private String id;

    @Column("user_id")
    private UUID userId;

    @Column("device_id")
    private String deviceId;

    @Column("status")
    private String status;

    @Column("activity_profile")
    private String activityProfile;

    @Column("connected_at")
    private Instant connectedAt;

    @Column("last_sent_at")
    private Instant lastSentAt;

    @Transient
    @Builder.Default
    private boolean isNew = false;

    @Override
    @Transient
    public boolean isNew() {
        return isNew;
    }
}