package com.hanainplan.domain.consult.repository;

import com.hanainplan.domain.consult.entity.ConsultationNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 상담 메모 레포지토리
 */
@Repository
public interface ConsultationNoteRepository extends JpaRepository<ConsultationNote, Long> {

    /**
     * 특정 상담의 모든 메모 조회
     */
    @Query("SELECT n FROM ConsultationNote n WHERE n.consultId = :consultId ORDER BY n.noteType, n.createdDate")
    List<ConsultationNote> findByConsultId(@Param("consultId") String consultId);

    /**
     * 특정 상담의 특정 타입 메모 조회
     */
    @Query("SELECT n FROM ConsultationNote n WHERE n.consultId = :consultId AND n.noteType = :noteType")
    Optional<ConsultationNote> findByConsultIdAndNoteType(@Param("consultId") String consultId, @Param("noteType") String noteType);

    /**
     * 특정 상담의 특정 사용자 메모 조회
     */
    @Query("SELECT n FROM ConsultationNote n WHERE n.consultId = :consultId AND n.userId = :userId AND n.noteType = :noteType")
    Optional<ConsultationNote> findByConsultIdAndUserIdAndNoteType(@Param("consultId") String consultId, @Param("userId") Long userId, @Param("noteType") String noteType);

    /**
     * 특정 상담의 공유 메모만 조회 (상담사가 작성한 공유 메모)
     */
    @Query("SELECT n FROM ConsultationNote n WHERE n.consultId = :consultId AND n.noteType = 'SHARED' ORDER BY n.createdDate")
    List<ConsultationNote> findSharedNotesByConsultId(@Param("consultId") String consultId);

    /**
     * 특정 상담의 특정 사용자 개인 메모 조회
     */
    @Query("SELECT n FROM ConsultationNote n WHERE n.consultId = :consultId AND n.userId = :userId AND n.noteType = 'PERSONAL'")
    Optional<ConsultationNote> findPersonalNoteByConsultIdAndUserId(@Param("consultId") String consultId, @Param("userId") Long userId);
}
