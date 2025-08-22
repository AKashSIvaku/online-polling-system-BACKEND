package com.pollsystem.springapp.repository;

import com.pollsystem.springapp.entity.Option;
import com.pollsystem.springapp.entity.Poll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OptionRepository extends JpaRepository<Option, Long> {
    List<Option> findByPollOrderById(Poll poll);
}
