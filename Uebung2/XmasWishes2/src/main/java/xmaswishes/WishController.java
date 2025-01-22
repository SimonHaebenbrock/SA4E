package xmaswishes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/wishes")
public class WishController {
    @Autowired
    private WishRepository wishRepository;

    @GetMapping
    public Iterable<Wish> getWishes() {
        return wishRepository.findAll();
    }

    @PostMapping
    public Wish addWish(@RequestBody Wish wish) {
        try {
            return wishRepository.save(wish);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error saving wish", e);
        }
    }

    @PutMapping("/{id}")
    public Wish updateWish(@PathVariable Long id, @RequestBody Wish updatedWish) {
        Optional<Wish> optionalWish = wishRepository.findById(id);
        if (optionalWish.isPresent()) {
            Wish wish = optionalWish.get();
            wish.setName(updatedWish.getName());
            wish.setDescription(updatedWish.getDescription());
            wish.setStatus(updatedWish.getStatus());
            return wishRepository.save(wish);
        } else {
            throw new RuntimeException("Wish not found");
        }
    }

    @DeleteMapping("/{id}")
    public void deleteWish(@PathVariable Long id) {
        wishRepository.deleteById(id);
    }
}