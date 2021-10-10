package com.kame.springboot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kame.springboot.model.Photo;


@Repository  // リポジトリもコンポーネントです
public interface PhotoRepository extends JpaRepository<Photo, Integer> {

}
