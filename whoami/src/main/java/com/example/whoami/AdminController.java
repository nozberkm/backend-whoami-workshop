package com.example.whoami;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {
    @Autowired
    private UserRepository uRep;
    @Autowired
    MessageRepository mRep;
    public static String adminId;

    @PostMapping("/api/v1/game/reset")
    public void resetGame(@RequestParam("password") String password) {
        adminCheck(password);
        GameController.isGameOn = false;
        GameController.readyCheck = false;
        GameController.endGame = false;
        uRep.findAll().forEach(user -> {
            user.resetState();
            uRep.save(user);
        });
        mRep.deleteAll();
    }

    @PostMapping("/api/v1/data/reset/users")
    public void resetUsers(@RequestParam("password") String password) {
        adminCheck(password);
        uRep.deleteAll();
    }

    @PostMapping("/api/v1/data/users")
    public Iterable<User> getUsers(@RequestParam("password") String password) {
        adminCheck(password);
        return uRep.findAll();
    }

    @PostMapping("/api/v1/data/users/count")
    public Long getUserCount(@RequestParam("password") String password) {
        adminCheck(password);
        return uRep.count();
    }

    @PostMapping("/api/v1/game/start")
    public void startGame(@RequestParam("password") String password) {
        adminCheck(password);

        if (!GameController.readyCheck)
            throw new IllegalGameStateException("Ready check not done.");

        if (uRep.count() % 2 != 0)
            throw new IllegalGameStateException("Odd number of players.");

        List<User> users = new ArrayList<User>();
        uRep.findAllReady().forEach(users::add);
        Collections.shuffle(users);

        for (int i = 0; i < users.size(); i += 2) {
            User firstUser = users.get(i);
            User secondUser = users.get(i + 1);
            firstUser.setAsking();
            firstUser.reverseTurn();
            firstUser.setPartner(secondUser);
            secondUser.setPartner(firstUser);
            uRep.save(firstUser);
            uRep.save(secondUser);
        }

        GameController.isGameOn = true;
    }

    @PostMapping("/api/v1/game/ready_check")
    public void askReady(@RequestParam("password") String password) {
        adminCheck(password);
        GameController.readyCheck = true;
    }

    @PostMapping("/api/v1/game/end")
    public void endGame(@RequestParam("password") String password) {
        adminCheck(password);
        GameController.endGame = true;
    }

    private void adminCheck(String password) throws UnauthorizedException {
        if (!password.equals(adminId))
            throw new UnauthorizedException();
    }
}