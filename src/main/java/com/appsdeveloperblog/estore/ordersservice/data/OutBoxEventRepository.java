package com.appsdeveloperblog.estore.ordersservice.data;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OutBoxEventRepository extends JpaRepository<OutboxEventEntity, UUID> {
    
}
