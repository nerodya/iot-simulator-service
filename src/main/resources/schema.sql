create table if not exists simulator_devices (
    id varchar(36) primary key,
    user_id uuid not null,
    device_id varchar(128) not null,
    status varchar(32) not null,
    activity_profile varchar(32) not null,
    connected_at timestamptz not null,
    last_sent_at timestamptz null
);

-- строго одно устройство на пользователя
create unique index if not exists ux_simulator_devices_user_id
    on simulator_devices(user_id);

create index if not exists ix_simulator_devices_status
    on simulator_devices(status);