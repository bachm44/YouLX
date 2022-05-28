package com.youlx.api.rest.offer;

import com.sun.security.auth.UserPrincipal;
import com.youlx.domain.offer.Offer;
import com.youlx.domain.offer.OfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
@RequiredArgsConstructor
public
class OfferModelAssembler implements RepresentationModelAssembler<Offer, EntityModel<OfferDto>> {
    private final OfferService offerService;

    @Override
    public EntityModel<OfferDto> toModel(Offer entity) {
        final var dto = new OfferDto(entity);
        return EntityModel.of(dto, createLinks(entity));
    }

    private List<Link> createLinks(Offer entity) {
        var links = new ArrayList<Link>();

        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof final UserDetails user) {
            final var principal = new UserPrincipal(user.getUsername());
            links.add(linkTo(methodOn(OfferController.class).get(principal, entity.getId())).withSelfRel());
            if (offerService.isClosable(user.getUsername(), entity)) {
                links.add(linkTo(methodOn(OfferController.class).close(principal, entity.getId())).withRel("close"));
            } else if (offerService.isPublishable(user.getUsername(), entity.getId())) {
                links.add(linkTo(methodOn(OfferController.class).publish(principal, entity.getId())).withRel("publish"));
            }
        }

        links.add(
                linkTo(methodOn(OfferController.class).getAllOpen(Pageable.unpaged())).withRel("allOpenOffers")
        );

        return links;
    }
}
