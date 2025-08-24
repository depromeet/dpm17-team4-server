package depromeet.lessonfour.server.auth.service;

import depromeet.lessonfour.server.auth.persist.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class UserUpdateService {

    private final UserRepository userRepository;

    public void updateRefreshToken(UUID userId, String newRefreshToken) {
        userRepository.findById(userId).ifPresent(user -> {
            user.storeRefreshToken(newRefreshToken);
        });
    }
}
