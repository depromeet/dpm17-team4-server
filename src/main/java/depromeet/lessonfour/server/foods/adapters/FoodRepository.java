package depromeet.lessonfour.server.foods.adapters;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import depromeet.lessonfour.server.foods.domain.entities.Food;

public interface FoodRepository extends JpaRepository<Food, UUID> {

  @Query(value = "SELECT * FROM foods ORDER BY RANDOM() LIMIT 5", nativeQuery = true)
  List<Food> findRandomFoods();
}
