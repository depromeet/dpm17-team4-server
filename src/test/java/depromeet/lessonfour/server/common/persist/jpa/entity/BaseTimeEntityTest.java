package depromeet.lessonfour.server.common.persist.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
class BaseTimeEntityTest {

    @Autowired
    private EntityManager em;

    @Entity
    @Getter
    @Setter
    @Table(name = "test_entity")
    static class TestEntity extends BaseTimeEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        
        private String name;
        
        public TestEntity() {}
        
        public TestEntity(String name) {
            this.name = name;
        }
    }

    @Test
    @DisplayName("엔티티가 생성될 때 생성시간이 저장되어야 한다")
    void givenTestEntity_whenSaved_thenCreatedTimeSaved() {
        // given
        TestEntity entity = new TestEntity("Test Name");
        assertThat(entity.getCreatedAt()).isNull();

        // when
        em.persist(entity);
        em.flush();
        em.clear();

        // then
        TestEntity saved = em.find(TestEntity.class, entity.getId());
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("엔티티가 생성될 때 수정시간이 저장되어야 한다")
    void givenTestEntity_whenSaved_thenUpdatedTimeSaved() {
        // given
        TestEntity entity = new TestEntity("Original Name");
        assertThat(entity.getUpdatedAt()).isNull();

        // when
        em.persist(entity);
        em.flush();
        em.clear();

        // then
        TestEntity saved = em.find(TestEntity.class, entity.getId());
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("엔티티가 수정될 때 수정시간이 갱신되어야 한다")
    void givenTestEntity_whenUpdated_thenUpdatedTimeChange() throws InterruptedException {
        // given
        TestEntity entity = new TestEntity("Original Name");
        em.persist(entity);
        em.flush();
        em.clear();

        TestEntity original = em.find(TestEntity.class, entity.getId());
        LocalDateTime originalUpdatedAt = original.getUpdatedAt();

        // when
        Thread.sleep(10);
        original.setName("Changed Name");
        em.flush();
        em.clear();

        TestEntity changed = em.find(TestEntity.class, entity.getId());
        LocalDateTime changedUpdatedAt = changed.getUpdatedAt();

        // then
        assertThat(changedUpdatedAt).isNotEqualTo(originalUpdatedAt);
    }
}
