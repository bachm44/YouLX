package com.youlx.infrastructure.tag;

import com.youlx.domain.offer.Offer;
import com.youlx.domain.offer.OfferRepository;
import com.youlx.domain.offer.OfferStatus;
import com.youlx.domain.tag.Tag;
import com.youlx.domain.tag.TagRepository;
import com.youlx.domain.user.UserRepository;
import com.youlx.domain.utils.exception.ApiConflictException;
import com.youlx.domain.utils.exception.ApiNotFoundException;
import com.youlx.domain.utils.hashId.HashId;
import com.youlx.domain.utils.hashId.HashIdImpl;
import com.youlx.infrastructure.JpaConfig;
import com.youlx.infrastructure.offer.JpaOfferRepository;
import com.youlx.infrastructure.offer.OfferTuple;
import com.youlx.infrastructure.user.UserTuple;
import org.hashids.Hashids;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.youlx.testUtils.Fixtures.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@Transactional
@ContextConfiguration(
        classes = {JpaConfig.class, HashIdImpl.class, Hashids.class},
        loader = AnnotationConfigContextLoader.class
)
@DataJpaTest
class TagRepositoryTests {
    @Autowired
    private TagRepository repository;

    @Autowired
    private OfferRepository offerRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JpaOfferRepository offerRepo;

    @Autowired
    private HashId hashId;

    @BeforeEach
    void setup() {
        repository.clear();
        userRepository.create(user);
        offerRepository.clear();
    }

    @AfterEach
    void teardown() {
        repository.clear();
        offerRepository.clear();
    }

    @Nested
    class GetTests {
        @Test
        void get() {
            final var tag1 = new Tag("1");
            final var tag2 = new Tag("2");
            repository.create(tag1);
            repository.create(tag2);

            assertEquals(List.of(tag1, tag2), repository.getAll());
        }

        @Test
        void getAfterAssignedInOrder() {
            final var tag1 = new Tag("1");
            final var tag2 = new Tag("2");
            final var offer = new Offer(null, null, null, null, null, Optional.empty(), user, List.of(), BigDecimal.TEN, LocalDateTime.now(), LocalDateTime.now(), Set.of());
            userRepository.create(user);
            final var createdOffer = offerRepo.saveAndFlush(new OfferTuple(offer, new UserTuple(user))).toDomain(hashId);
            repository.create(tag1);
            repository.create(tag2);

            repository.assignToOffer(createdOffer.getId(), tag1);

            assertEquals(List.of(tag1, tag2), repository.getAll());
        }

        @Test
        void getAfterAssignedReversed() {
            final var tag1 = new Tag("1");
            final var tag2 = new Tag("2");
            final var offer = new Offer(null, null, null, null, null, Optional.empty(), user, List.of(), BigDecimal.TEN, LocalDateTime.now(), LocalDateTime.now(), Set.of());
            userRepository.create(user);
            final var createdOffer = offerRepo.saveAndFlush(new OfferTuple(offer, new UserTuple(user))).toDomain(hashId);
            repository.create(tag1);
            repository.create(tag2);

            repository.assignToOffer(createdOffer.getId(), tag2);

            assertEquals(List.of(tag2, tag1), repository.getAll());
        }
    }

    @Nested
    class CreateTests {
        @Test
        void conflict() {
            final var tag = new Tag("a");
            repository.create(tag);
            assertThrows(ApiConflictException.class, () -> repository.create(tag));
        }

        @Test
        void create() {
            final var tags = List.of(new Tag("a"), new Tag("b"));
            tags.forEach(tag -> repository.create(tag));
            assertEquals(tags, repository.getAll());
        }
    }

    @Nested
    class AssignToOfferTests {
        @Test
        void offerNotFound() {
            assertThrows(ApiNotFoundException.class, () -> repository.assignToOffer("asdf", new Tag("sdf")));
        }

        @Test
        void tagNotFound() {
            userRepository.create(user);
            offerRepo.save(new OfferTuple(offer, new UserTuple(user)));

            assertThrows(ApiNotFoundException.class, () -> repository.assignToOffer("asdf", new Tag("asdf")));
        }

        @Test
        @Commit
        void tagAlreadyAssigned() {
            final var offer = new Offer("8", "", "", OfferStatus.OPEN, LocalDateTime.now(), Optional.empty(), user, List.of(), BigDecimal.TEN, LocalDateTime.now(), LocalDateTime.now(), Set.of());
            userRepository.create(user);
            final var createdOffer = offerRepo.saveAndFlush(new OfferTuple(offer, new UserTuple(user))).toDomain(hashId);
            repository.create(tag);

            repository.assignToOffer(createdOffer.getId(), tag);
            assertThrows(ApiConflictException.class, () -> repository.assignToOffer(createdOffer.getId(), tag));
        }

        @Test
        @Commit
        void assign() {
            final var offer = new Offer("9", null, null, null, null, Optional.empty(), user, List.of(), BigDecimal.TEN, LocalDateTime.now(), LocalDateTime.now(), Set.of());
            userRepository.create(user);
            final var createdOffer = offerRepo.saveAndFlush(new OfferTuple(offer, new UserTuple(user))).toDomain(hashId);
            repository.create(tag);

            assertEquals(Set.of(), offerRepository.findById(createdOffer.getId()).get().getTags());
            repository.assignToOffer(createdOffer.getId(), tag);

            assertEquals(Set.of(tag), offerRepository.findById(createdOffer.getId()).get().getTags());
        }
    }
}
