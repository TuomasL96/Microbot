package net.runelite.client.plugins.microbot.scout;

import lombok.Value;
import net.runelite.api.ItemComposition;
import net.runelite.api.PlayerComposition;
import net.runelite.api.kit.KitType;
import net.runelite.api.Player;
import net.runelite.client.RuneLite;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.util.AsyncBufferedImage;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


public class EquipmentFinder {
    private final Map<KitType, AsyncBufferedImage> equipmentImages = new HashMap<>();
    private final Set<KitType> pendingImages = new HashSet<>();
    private boolean isLoading = false;
    private final File tempDir;
    private final List<File> tempFiles = new ArrayList<>();

    public EquipmentFinder() {
        // Create a temporary directory for our image files
        try {
            tempDir = Files.createTempDirectory("equipment_images").toFile();
            tempDir.deleteOnExit();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temporary directory for equipment images", e);
        }
    }

    // Method to get equipment images and wait until they're all loaded
    public Map<KitType, AsyncBufferedImage> getPlayerEquipmentAndWait(Player player) {
        CompletableFuture<Map<KitType, AsyncBufferedImage>> future = new CompletableFuture<>();

        getPlayerEquipment(player, () -> {
            future.complete(new HashMap<>(equipmentImages));
        });

        try {
            return future.get(3, TimeUnit.SECONDS); // Adding timeout to prevent infinite waiting
        } catch (Exception e) {
            Microbot.log("Error waiting for equipment images: " + e.getMessage());
            return new HashMap<>(equipmentImages);
        }
    }

    public void getPlayerEquipment(Player player, Runnable callback) {
        // Clear previous state
        synchronized (equipmentImages) {
            equipmentImages.clear();
            pendingImages.clear();
        }

        isLoading = true;

        PlayerInfo p = getPlayerInfo(player.getId());
        if (p == null) {
            Microbot.log("Error getting player @ GetMinimumRisk");
            isLoading = false;
            if (callback != null) callback.run();
            return;
        }

        boolean hasEquipment = false;

        for (KitType kitType : KitType.values()) {
            int itemId = p.getPlayerComposition().getEquipmentId(kitType);

            if (itemId > 0) {
                hasEquipment = true;
                synchronized (pendingImages) {
                    pendingImages.add(kitType);
                }

                AsyncBufferedImage image = Microbot.getItemManager().getImage(itemId);
                image.onLoaded(() -> {
                    synchronized (equipmentImages) {
                        equipmentImages.put(kitType, image);

                        synchronized (pendingImages) {
                            pendingImages.remove(kitType);
                            if (pendingImages.isEmpty()) {
                                isLoading = false;
                                if (callback != null) callback.run();
                            }
                        }
                    }
                });
            }
        }

        // If no equipment was found, call the callback immediately
        if (!hasEquipment) {
            isLoading = false;
            if (callback != null) callback.run();
        }
    }

    public Map<KitType, AsyncBufferedImage> getEquipmentImages() {
        return new HashMap<>(equipmentImages); // Return a copy to prevent modification
    }

    public boolean isLoading() {
        return isLoading;
    }

    @Value
    private static class PlayerInfo {
        int id;
        String name;
        PlayerComposition playerComposition;
    }

    private PlayerInfo getPlayerInfo(int id) {
        Player player = Microbot.getClient().getTopLevelWorldView().players().byIndex(id);
        if (player != null) {
            return new PlayerInfo(player.getId(), player.getName(), player.getPlayerComposition());
        }
        return null;
    }

    public List<String> getEquipmentImagePaths() throws IOException {
        List<String> imagePaths = new ArrayList<>();
        cleanupTempFiles();
        synchronized (equipmentImages) {
            for (Map.Entry<KitType, AsyncBufferedImage> entry : equipmentImages.entrySet()) {
                KitType kitType = entry.getKey();
                AsyncBufferedImage image = entry.getValue();

                File imageFile = saveToFile(image, "equipment_" + kitType.name() + ".png");
                tempFiles.add(imageFile);
                imagePaths.add(imageFile.getAbsolutePath());
            }
        }

        return imagePaths;
    }

    // Method to create a combined equipment image and return its path
    public String createCombinedEquipmentImagePath() throws IOException {
        if (equipmentImages.isEmpty()) {
            return null;
        }
        cleanupTempFiles();
        // Create a single image with all equipment
        int imageWidth = 32; // Assuming item images are 32x32
        int imageHeight = 32;
        int padding = 2;
        int totalImages = equipmentImages.size();

        // Create a new image with enough space for all equipment items in a row
        BufferedImage combined = new BufferedImage(
                (imageWidth + padding) * totalImages - padding,
                imageHeight,
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D g2d = combined.createGraphics();
        g2d.setBackground(new Color(0, 0, 0, 0)); // Transparent background
        g2d.clearRect(0, 0, combined.getWidth(), combined.getHeight());

        // Draw each equipment image
        int x = 0;
        for (AsyncBufferedImage image : equipmentImages.values()) {
            g2d.drawImage(image, x, 0, null);
            x += imageWidth + padding;
        }

        g2d.dispose();

        File combinedFile = new File(tempDir, "combined_equipment.png");
        ImageIO.write(combined, "png", combinedFile);
        tempFiles.add(combinedFile);
        return combinedFile.getAbsolutePath();
    }

    private File saveToFile(AsyncBufferedImage image, String fileName) throws IOException {
        File imageFile = new File(tempDir, fileName);
        ImageIO.write(image, "png", imageFile);
        return imageFile;

    }
    public void cleanupTempFiles() {
        for (File file : tempFiles) {
            if (file.exists()) {
                file.delete();
            }
        }
        tempFiles.clear();
    }
}


