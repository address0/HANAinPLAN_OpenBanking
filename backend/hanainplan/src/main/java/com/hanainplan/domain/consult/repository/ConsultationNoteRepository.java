package com.hanainplan.domain.consult.repository;

import com.hanainplan.domain.consult.entity.ConsultationNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultationNoteRepository extends JpaRepository<ConsultationNote, Long> {

    @Query("SELECT n FROM ConsultationNote n WHERE n.consultId = :consultId ORDER BY n.noteType, n.createdDate")
    List<ConsultationNote> findByConsultId(@Param("consultId") String consultId);

    @Query("SELECT n FROM ConsultationNote n WHERE n.consultId = :consultId AND n.noteType = :noteType")
    Optional<ConsultationNote> findByConsultIdAndNoteType(@Param("consultId") String consultId, @Param("noteType") String noteType);

    @Query("SELECT n FROM ConsultationNote n WHERE n.consultId = :consultId AND n.userId = :userId AND n.noteType = :noteType")
    Optional<ConsultationNote> findByConsultIdAndUserIdAndNoteType(@Param("consultId") String consultId, @Param("userId") Long userId, @Param("noteType") String noteType);

    @Query("SELECT n FROM ConsultationNote n WHERE n.consultId = :consultId AND n.noteType = 'SHARED' ORDER BY n.createdDate")
    List<ConsultationNote> findSharedNotesByConsultId(@Param("consultId") String consultId);

    @Query("SELECT n FROM ConsultationNote n WHERE n.consultId = :consultId AND n.userId = :userId AND n.noteType = 'PERSONAL'")
    Optional<ConsultationNote> findPersonalNoteByConsultIdAndUserId(@Param("consultId") String consultId, @Param("userId") Long userId);
}