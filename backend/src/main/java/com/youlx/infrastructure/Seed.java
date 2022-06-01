package com.youlx.infrastructure;

import com.github.javafaker.Faker;
import com.youlx.api.config.SecurityRoles;
import com.youlx.domain.offer.Offer;
import com.youlx.domain.offer.OfferClose;
import com.youlx.domain.offer.OfferCloseReason;
import com.youlx.domain.offer.OfferRepository;
import com.youlx.domain.photo.Photo;
import com.youlx.domain.tag.Tag;
import com.youlx.domain.tag.TagRepository;
import com.youlx.domain.user.User;
import com.youlx.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class Seed implements ApplicationRunner {
    private final OfferRepository offerRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Random random = new Random(1L);
    private static final Faker faker = new Faker(random);
    private static final int NUMBER_OF_TAGS = 10;
    private static final int PHOTOS_PER_OFFER = 3;
    private static final int OFFER_COUNT = 9;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        final var users = List.of(userFrom("user1"), userFrom("user2"));
        users.forEach(userRepository::create);

        final var photos = List.of(photoFrom("fixtures/photo1.jpg"), photoFrom("fixtures/photo2.jpg"), photoFrom("fixtures/photo3.jpg"), photoFrom("fixtures/photo4.jpg"), photoFrom("fixtures/photo5.jpg"), photoFrom("fixtures/photo6.jpg"), photoFrom("fixtures/photo7.jpg"));

        final var tags = IntStream.range(0, NUMBER_OF_TAGS).mapToObj(i -> faker.commerce().productName()).map(Tag::new).toList();
        tags.forEach(tagRepository::create);

        for (final var user : users) {
            IntStream.range(0, OFFER_COUNT).forEach(i -> {
                createDraftOffer(user, photos, tags);
                createOpenOffer(user, photos, tags);
                createClosedOffer(user, photos, tags);
            });
        }
    }

    private void createOpenOffer(User user, List<Photo> photos, List<Tag> tags) {
        final var open = offerRepository.create(offerFrom(user, photos));
        offerRepository.publish(open.getId());
        assignTagsToOffer(tags, open.getId());
    }

    private void createDraftOffer(User user, List<Photo> photos, List<Tag> tags) {
        final var draft = offerRepository.create(offerFrom(user, photos));
        assignTagsToOffer(tags, draft.getId());
    }

    private void createClosedOffer(User user, List<Photo> photos, List<Tag> tags) {
        final var closed = offerRepository.create(offerFrom(user, photos));
        offerRepository.publish(closed.getId());
        offerRepository.close(closed.getId(), new OfferClose(OfferCloseReason.MANUAL));
        assignTagsToOffer(tags, closed.getId());
    }

    private void assignTagsToOffer(List<Tag> allTags, String offerId) {
//        tagRepository.assignToOffer(offerId, allTags.get(faker.random().nextInt(allTags.size())));
//        tagRepository.assignToOffer(offerId, allTags.get(faker.random().nextInt(allTags.size())));
    }

    private User userFrom(String username) {
        return new User(
                List.of(new SimpleGrantedAuthority(SecurityRoles.USER.name())),
                faker.name().firstName(),
                faker.name().lastName(),
                username + "@mail.com",
                passwordEncoder.encode(username),
                username,
                "+48555555555"
        );
    }

    private Photo photoFrom(String path) throws IOException {
        return new Photo(new ClassPathResource(path).getInputStream().readAllBytes());
    }

    private Offer offerFrom(User user, List<Photo> photos) {
        final var name = faker.commerce().productName();
        final var description = faker.lorem().paragraph(80);
        final var selectedPhotos = IntStream.range(0, PHOTOS_PER_OFFER).mapToObj(i -> faker.random().nextInt(photos.size())).map(photos::get).toList();
        final var price = BigDecimal.valueOf(faker.number().randomDouble(2, 1, 200));

        return new Offer(name, description, user, selectedPhotos, price);
    }
}
