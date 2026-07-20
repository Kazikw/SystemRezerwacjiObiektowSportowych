package com.github.kazikw.boisgo.service;

import com.github.kazikw.boisgo.domain.FriendRelation;
import com.github.kazikw.boisgo.domain.User;
import com.github.kazikw.boisgo.repository.FriendRelationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRelationRepository friendRelationRepository;
    private final UserService userService;

    public List<User> getFriends(Long userId) {
        return friendRelationRepository.findAllByUserId(userId).stream()
                .map(FriendRelation::getUserB)
                .toList();
    }

    @Transactional
    public void addFriend(User currentUser, String friendEmail) {
        User friend = userService.getByEmail(friendEmail);

        if (friend.getId().equals(currentUser.getId())) {
            throw new IllegalStateException("Nie możesz dodać samego siebie do znajomych.");
        }
        if (friendRelationRepository.existsByUserA_IdAndUserB_Id(currentUser.getId(), friend.getId())) {
            throw new IllegalStateException("Ten użytkownik jest już Twoim znajomym.");
        }

        friendRelationRepository.save(FriendRelation.builder().userA(currentUser).userB(friend).build());
        friendRelationRepository.save(FriendRelation.builder().userA(friend).userB(currentUser).build());
    }

    @Transactional
    public void removeFriend(User currentUser, Long friendId) {
        friendRelationRepository.findByUserA_IdAndUserB_Id(currentUser.getId(), friendId)
                .ifPresent(friendRelationRepository::delete);
        friendRelationRepository.findByUserA_IdAndUserB_Id(friendId, currentUser.getId())
                .ifPresent(friendRelationRepository::delete);
    }
}