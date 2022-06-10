package com.infrastructure.conversation;

import com.infrastructure.offer.OfferTuple;
import com.infrastructure.user.UserTuple;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaConversationRepository extends JpaRepository<ConversationTuple, Long> {
    List<ConversationTuple> findAllByBrowserAndOffer(UserTuple browser, OfferTuple offer);

    List<ConversationTuple> findAllByPosterAndOffer(UserTuple browser, OfferTuple offer);

    boolean existsByOfferAndBrowser(OfferTuple offer, UserTuple browser);
    boolean existsByOfferAndPoster(OfferTuple offer, UserTuple poster);

    List<ConversationTuple> findAllByPoster(UserTuple user);
    List<ConversationTuple> findAllByBrowser(UserTuple user);
}
