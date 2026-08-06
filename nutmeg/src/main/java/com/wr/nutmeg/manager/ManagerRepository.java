package com.wr.nutmeg.manager;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ManagerRepository extends JpaRepository<Manager, UUID> {

    @Query("SELECT m FROM Manager m WHERE m.username = :login OR LOWER(m.email) = LOWER(:login)")
    Optional<Manager> findByUsernameOrEmail(@Param("login") String login);
}
