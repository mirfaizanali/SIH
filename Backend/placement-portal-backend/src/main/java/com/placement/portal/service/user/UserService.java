package com.placement.portal.service.user;

import com.placement.portal.domain.User;
import com.placement.portal.dto.response.UserDto;
import com.placement.portal.exception.EntityNotFoundException;
import com.placement.portal.mapper.EntityMapper;
import com.placement.portal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EntityMapper entityMapper;

    /**
     * Retrieves a user by their UUID.
     *
     * @param id the user UUID
     * @return the mapped {@link UserDto}
     * @throws EntityNotFoundException if no user exists with the given id
     */
    public UserDto getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User", id));
        return entityMapper.toUserDto(user);
    }

    /**
     * Returns a paginated list of all users (admin only — caller must enforce role via @PreAuthorize).
     *
     * @param pageable pagination parameters
     * @return page of {@link UserDto}
     */
    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(entityMapper::toUserDto);
    }

    /**
     * Deactivates a user account by setting {@code isActive = false}.
     *
     * @param id the user UUID
     * @throws EntityNotFoundException if no user exists with the given id
     */
    @Transactional
    public void deactivateUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User", id));
        user.setActive(false);
        userRepository.save(user);
        log.info("User {} deactivated", id);
    }
}
