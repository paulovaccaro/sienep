package edu.utec.sienep.repository;

import edu.utec.sienep.entity.TeleITR;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeleITRRepository extends JpaRepository<TeleITR, Integer> {

    List<TeleITR> findByItr_IdItr(Integer idItr);
}
