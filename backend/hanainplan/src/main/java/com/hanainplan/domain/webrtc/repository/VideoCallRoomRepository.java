package com.hanainplan.domain.webrtc.repository;

import com.hanainplan.domain.webrtc.entity.VideoCallRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoCallRoomRepository extends JpaRepository<VideoCallRoom, Long> {

    Optional<VideoCallRoom> findByRoomId(String roomId);

    @Query("SELECT v FROM VideoCallRoom v WHERE (v.callerId = :userId OR v.calleeId = :userId) AND v.status = :status")
    List<VideoCallRoom> findByUserIdAndStatus(@Param("userId") Long userId, 
                                            @Param("status") VideoCallRoom.CallStatus status);

    @Query("SELECT v FROM VideoCallRoom v WHERE (v.callerId = :callerId AND v.calleeId = :calleeId) " +
           "OR (v.callerId = :calleeId AND v.calleeId = :callerId)")
    List<VideoCallRoom> findByCallerIdAndCalleeId(@Param("callerId") Long callerId, 
                                                @Param("calleeId") Long calleeId);

    @Query("SELECT v FROM VideoCallRoom v WHERE v.status = 'WAITING' AND " +
           "(v.callerId = :userId OR v.calleeId = :userId)")
    Optional<VideoCallRoom> findActiveCallByUserId(@Param("userId") Long userId);
} 