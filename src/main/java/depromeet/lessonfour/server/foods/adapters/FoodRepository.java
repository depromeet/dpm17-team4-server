package depromeet.lessonfour.server.foods.adapters;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import depromeet.lessonfour.server.foods.domain.Food;

public interface FoodRepository extends JpaRepository<Food, Long> {

  List<Food> findAllByIdIn(List<Long> foodIds);
}
