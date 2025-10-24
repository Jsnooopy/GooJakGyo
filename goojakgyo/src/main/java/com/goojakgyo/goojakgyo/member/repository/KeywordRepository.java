package com.goojakgyo.goojakgyo.member.repository;

import com.goojakgyo.goojakgyo.member.domain.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KeywordRepository extends JpaRepository<Keyword, Long> {

}
