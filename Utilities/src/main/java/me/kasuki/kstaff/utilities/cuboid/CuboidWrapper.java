package me.kasuki.kstaff.utilities.cuboid;

import com.cryptomorin.xseries.XMaterial;
import lombok.Getter;
import me.kasuki.kstaff.utilities.location.LocationOuterClass;
import org.bukkit.*;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Wrapper for cuboid data and behavior.
 */
public class CuboidWrapper implements Iterable<Block>, Cloneable {

    /**
     * Stores cuboid.
     */
    @Getter
    private final CuboidOuterClass.Cuboid cuboid;

    /**
     * Stores world name.
     */
    private final String worldName;

    /**
     * Stores x1.
     */
    private final int x1;

    /**
     * Stores y1.
     */
    private final int y1;

    /**
     * Stores z1.
     */
    private final int z1;

    /**
     * Stores x2.
     */
    private final int x2;

    /**
     * Stores y2.
     */
    private final int y2;

    /**
     * Stores z2.
     */
    private final int z2;

    /**
     * Creates a new CuboidWrapper instance.
     */
    public CuboidWrapper(CuboidOuterClass.Cuboid cuboid) {
        this.cuboid = cuboid;
        LocationOuterClass.Location firstLocation = cuboid.getFirstLocation();
        LocationOuterClass.Location secondLocation = cuboid.getSecondLocation();

        if (!firstLocation.getWorldId().equals(secondLocation.getWorldId())) {
            throw new IllegalArgumentException("Locations must be on the same world");
        }

        this.worldName = firstLocation.getWorldId();

        this.x1 = Math.min(firstLocation.getX(), secondLocation.getX());
        this.y1 = Math.min(firstLocation.getY(), secondLocation.getY());
        this.z1 = Math.min(firstLocation.getZ(), secondLocation.getZ());
        this.x2 = Math.max(firstLocation.getX(), secondLocation.getX());
        this.y2 = Math.max(firstLocation.getY(), secondLocation.getY());
        this.z2 = Math.max(firstLocation.getZ(), secondLocation.getZ());

        this.location1 = new Location(Bukkit.getServer().getWorld(worldName), x1, y1, z1);
        this.location2 = new Location(Bukkit.getServer().getWorld(worldName), x2, y2, z2);
    }

    /**
     * Construct a CuboidWrapper given two Location objects which represent any two corners of the
     * CuboidWrapper. Note: The 2 locations must be on the same world.
     *
     * @param l1 - One of the corners
     * @param l2 - The other corner
     */
    private Location location1;

    /**
     * Stores location2.
     */
    private Location location2;

    /**
     * Creates a new CuboidWrapper instance.
     */
    public CuboidWrapper(Location l1, Location l2) {
        if (!l1.getWorld().equals(l2.getWorld())) {
            throw new IllegalArgumentException("Locations must be on the same world");
        }

        LocationOuterClass.Location firstProto =
                LocationOuterClass.Location.newBuilder()
                        .setX(l1.getBlockX())
                        .setY(l1.getBlockY())
                        .setZ(l1.getBlockZ())
                        .setWorldId(l1.getWorld().getName())
                        .build();
        LocationOuterClass.Location secondProto =
                LocationOuterClass.Location.newBuilder()
                        .setX(l2.getBlockX())
                        .setY(l2.getBlockY())
                        .setZ(l2.getBlockZ())
                        .setWorldId(l2.getWorld().getName())
                        .build();
        this.cuboid =
                CuboidOuterClass.Cuboid.newBuilder()
                        .setFirstLocation(firstProto)
                        .setSecondLocation(secondProto)
                        .build();

        this.location1 = l1;
        this.location2 = l2;

        this.worldName = l1.getWorld().getName();
        this.x1 = Math.min(l1.getBlockX(), l2.getBlockX());
        this.y1 = Math.min(l1.getBlockY(), l2.getBlockY());
        this.z1 = Math.min(l1.getBlockZ(), l2.getBlockZ());
        this.x2 = Math.max(l1.getBlockX(), l2.getBlockX());
        this.y2 = Math.max(l1.getBlockY(), l2.getBlockY());
        this.z2 = Math.max(l1.getBlockZ(), l2.getBlockZ());
    }

    /**
     * Construct a one-block CuboidWrapper at the given Location of the CuboidWrapper.
     *
     * @param l1 location of the CuboidWrapper
     */
    public CuboidWrapper(Location l1) {
        this(l1, l1);
    }

    /**
     * Copy constructor.
     *
     * @param other - The CuboidWrapper to copy
     */
    public CuboidWrapper(CuboidWrapper other) {
        this(other.getWorld().getName(), other.x1, other.y1, other.z1, other.x2, other.y2, other.z2);
    }

    /**
     * Construct a CuboidWrapper in the given World and xyz co-ordinates
     *
     * @param world - The CuboidWrapper's world
     * @param x1    - X co-ordinate of corner 1
     * @param y1    - Y co-ordinate of corner 1
     * @param z1    - Z co-ordinate of corner 1
     * @param x2    - X co-ordinate of corner 2
     * @param y2    - Y co-ordinate of corner 2
     * @param z2    - Z co-ordinate of corner 2
     */
    public CuboidWrapper(World world, int x1, int y1, int z1, int x2, int y2, int z2) {
        this.worldName = world.getName();
        this.x1 = Math.min(x1, x2);
        this.x2 = Math.max(x1, x2);
        this.y1 = Math.min(y1, y2);
        this.y2 = Math.max(y1, y2);
        this.z1 = Math.min(z1, z2);
        this.z2 = Math.max(z1, z2);

        LocationOuterClass.Location firstProto =
                LocationOuterClass.Location.newBuilder().setX(x1).setY(y1).setZ(z1).build();
        LocationOuterClass.Location secondProto =
                LocationOuterClass.Location.newBuilder().setX(x2).setY(y2).setZ(z2).build();

        this.cuboid =
                CuboidOuterClass.Cuboid.newBuilder()
                        .setFirstLocation(firstProto)
                        .setSecondLocation(secondProto)
                        .build();
    }

    /**
     * Construct a CuboidWrapper in the given world name and xyz co-ordinates.
     *
     * @param worldName - The CuboidWrapper's world name
     * @param x1        - X co-ordinate of corner 1
     * @param y1        - Y co-ordinate of corner 1
     * @param z1        - Z co-ordinate of corner 1
     * @param x2        - X co-ordinate of corner 2
     * @param y2        - Y co-ordinate of corner 2
     * @param z2        - Z co-ordinate of corner 2
     */
    private CuboidWrapper(String worldName, int x1, int y1, int z1, int x2, int y2, int z2) {
        this.worldName = worldName;
        this.x1 = Math.min(x1, x2);
        this.x2 = Math.max(x1, x2);
        this.y1 = Math.min(y1, y2);
        this.y2 = Math.max(y1, y2);
        this.z1 = Math.min(z1, z2);
        this.z2 = Math.max(z1, z2);

        LocationOuterClass.Location firstProto =
                LocationOuterClass.Location.newBuilder().setX(x1).setY(y1).setZ(z1).build();
        LocationOuterClass.Location secondProto =
                LocationOuterClass.Location.newBuilder().setX(x2).setY(y2).setZ(z2).build();

        this.cuboid =
                CuboidOuterClass.Cuboid.newBuilder()
                        .setFirstLocation(firstProto)
                        .setSecondLocation(secondProto)
                        .build();
    }

    /**
     * Get the north-east corner of the CuboidWrapper.
     *
     * @return Location of the north-east corner
     */
    public Location getNorthEast() {
        World world = this.getWorld();
        return new Location(world, this.x1, this.y1, this.z1);
    }

    /**
     * Get the south-west corner of the CuboidWrapper.
     *
     * @return Location of the south-west corner
     */
    public Location getSouthWest() {
        World world = this.getWorld();
        return new Location(world, this.x2, this.y2, this.z2);
    }

    /**
     * Get the south-east corner of the CuboidWrapper.
     *
     * @return Location of the south-east corner
     */
    public Location getSouthEast() {
        World world = this.getWorld();
        return new Location(world, this.x1, this.y2, this.z2);
    }

    /**
     * Get the north-west corner of the CuboidWrapper.
     *
     * @return Location of the north-west corner
     */
    public Location getNorthWest() {
        World world = this.getWorld();
        return new Location(world, this.x2, this.y1, this.z1);
    }

    /**
     * Get the blocks in the CuboidWrapper.
     *
     * @return The blocks in the CuboidWrapper
     */
    public List<Block> getBlocks() {
        Iterator<Block> blockI = this.iterator();
        List<Block> copy = new ArrayList<>();
        while (blockI.hasNext()) copy.add(blockI.next());
        return copy;
    }

    /**
     * Gets blocks.
     */
    public List<Block> getBlocks(int y) {
        Iterator<Block> blockI = this.iterator();
        List<Block> copy = new ArrayList<Block>();
        while (blockI.next().getY() == y && blockI.hasNext()) copy.add(blockI.next());
        return copy;
    }

    /**
     * Get the the centre of the CuboidWrapper.
     *
     * @return Location at the centre of the CuboidWrapper
     */
    public Location getCenter() {
        int x1 = this.getUpperX() + 1;
        int y1 = this.getUpperY() + 1;
        int z1 = this.getUpperZ() + 1;
        return new Location(
                this.getWorld(),
                this.getLowerX() + (x1 - this.getLowerX()) / 2.0,
                this.getLowerY() + (y1 - this.getLowerY()) / 2.0,
                this.getLowerZ() + (z1 - this.getLowerZ()) / 2.0);
    }

    /**
     * Get the CuboidWrapper's world.
     *
     * @return The World object representing this CuboidWrapper's world
     * @throws IllegalStateException if the world is not loaded
     */
    public World getWorld() {
        World world = Bukkit.getWorld(this.worldName);
        if (world == null)
            throw new IllegalStateException("World '" + this.worldName + "' is not loaded");
        return world;
    }

    /**
     * Get the size of this CuboidWrapper along the X axis
     *
     * @return Size of CuboidWrapper along the X axis
     */
    public int getSizeX() {
        return (this.x2 - this.x1) + 1;
    }

    /**
     * Get the size of this CuboidWrapper along the Y axis
     *
     * @return Size of CuboidWrapper along the Y axis
     */
    public int getSizeY() {
        return (this.y2 - this.y1) + 1;
    }

    /**
     * Get the size of this CuboidWrapper along the Z axis
     *
     * @return Size of CuboidWrapper along the Z axis
     */
    public int getSizeZ() {
        return (this.z2 - this.z1) + 1;
    }

    /**
     * Gets the area of this {@link CuboidWrapper}.
     *
     * @return the {@link CuboidWrapper} area
     */
    public int getArea() {
        Location min = this.getLocation1();
        Location max = this.getLocation2();
        return ((max.getBlockX() - min.getBlockX() + 1) * (max.getBlockZ() - min.getBlockZ() + 1));
    }

    /**
     * Get the minimum X co-ordinate of this CuboidWrapper
     *
     * @return the minimum X co-ordinate
     */
    public int getLowerX() {
        return this.x1;
    }

    /**
     * Get the minimum Y co-ordinate of this CuboidWrapper
     *
     * @return the minimum Y co-ordinate
     */
    public int getLowerY() {
        return this.y1;
    }

    /**
     * Get the minimum Z co-ordinate of this CuboidWrapper
     *
     * @return the minimum Z co-ordinate
     */
    public int getLowerZ() {
        return this.z1;
    }

    /**
     * Get the maximum X co-ordinate of this CuboidWrapper
     *
     * @return the maximum X co-ordinate
     */
    public int getUpperX() {
        return this.x2;
    }

    /**
     * Get the maximum Y co-ordinate of this CuboidWrapper
     *
     * @return the maximum Y co-ordinate
     */
    public int getUpperY() {
        return this.y2;
    }

    /**
     * Get the maximum Z co-ordinate of this CuboidWrapper
     *
     * @return the maximum Z co-ordinate
     */
    public int getUpperZ() {
        return this.z2;
    }

    /**
     * Get the Blocks at the eight corners of the CuboidWrapper.
     *
     * @return array of Block objects representing the CuboidWrapper corners
     */
    public Block[] corners() {
        Block[] res = new Block[8];
        World w = this.getWorld();
        res[0] = w.getBlockAt(this.x1, this.y1, this.z1);
        res[1] = w.getBlockAt(this.x1, this.y1, this.z2);
        res[2] = w.getBlockAt(this.x1, this.y2, this.z1);
        res[3] = w.getBlockAt(this.x1, this.y2, this.z2);
        res[4] = w.getBlockAt(this.x2, this.y1, this.z1);
        res[5] = w.getBlockAt(this.x2, this.y1, this.z2);
        res[6] = w.getBlockAt(this.x2, this.y2, this.z1);
        res[7] = w.getBlockAt(this.x2, this.y2, this.z2);
        return res;
    }

    /**
     * Expand the CuboidWrapper in the given direction by the given amount. Negative amounts will
     * shrink the CuboidWrapper in the given direction. Shrinking a cuboid's face past the opposite
     * face is not an error and will return a valid CuboidWrapper.
     *
     * @param dir    - The direction in which to expand
     * @param amount - The number of blocks by which to expand
     * @return A new CuboidWrapper expanded by the given direction and amount
     */
    public CuboidWrapper expand(CuboidDirection dir, int amount) {
        switch (dir) {
            case North:
                return new CuboidWrapper(
                        this.worldName, this.x1 - amount, this.y1, this.z1, this.x2, this.y2, this.z2);
            case South:
                return new CuboidWrapper(
                        this.worldName, this.x1, this.y1, this.z1, this.x2 + amount, this.y2, this.z2);
            case East:
                return new CuboidWrapper(
                        this.worldName, this.x1, this.y1, this.z1 - amount, this.x2, this.y2, this.z2);
            case West:
                return new CuboidWrapper(
                        this.worldName, this.x1, this.y1, this.z1, this.x2, this.y2, this.z2 + amount);
            case Down:
                return new CuboidWrapper(
                        this.worldName, this.x1, this.y1 - amount, this.z1, this.x2, this.y2, this.z2);
            case Up:
                return new CuboidWrapper(
                        this.worldName, this.x1, this.y1, this.z1, this.x2, this.y2 + amount, this.z2);
            default:
                throw new IllegalArgumentException("Invalid direction " + dir);
        }
    }

    /**
     * Shift the CuboidWrapper in the given direction by the given amount.
     *
     * @param dir    - The direction in which to shift
     * @param amount - The number of blocks by which to shift
     * @return A new CuboidWrapper shifted by the given direction and amount
     */
    public CuboidWrapper shift(CuboidDirection dir, int amount) {
        return expand(dir, amount).expand(dir.opposite(), -amount);
    }

    /**
     * Outset (grow) the CuboidWrapper in the given direction by the given amount.
     *
     * @param dir    - The direction in which to outset (must be Horizontal, Vertical, or Both)
     * @param amount - The number of blocks by which to outset
     * @return A new CuboidWrapper outset by the given direction and amount
     */
    public CuboidWrapper outset(CuboidDirection dir, int amount) {
        CuboidWrapper c;
        switch (dir) {
            case Horizontal:
                c =
                        expand(CuboidDirection.North, amount)
                                .expand(CuboidDirection.South, amount)
                                .expand(CuboidDirection.East, amount)
                                .expand(CuboidDirection.West, amount);
                break;
            case Vertical:
                c = expand(CuboidDirection.Down, amount).expand(CuboidDirection.Up, amount);
                break;
            case Both:
                c = outset(CuboidDirection.Horizontal, amount).outset(CuboidDirection.Vertical, amount);
                break;
            default:
                throw new IllegalArgumentException("Invalid direction " + dir);
        }
        return c;
    }

    /**
     * Inset (shrink) the CuboidWrapper in the given direction by the given amount. Equivalent to
     * calling outset() with a negative amount.
     *
     * @param dir    - The direction in which to inset (must be Horizontal, Vertical, or Both)
     * @param amount - The number of blocks by which to inset
     * @return A new CuboidWrapper inset by the given direction and amount
     */
    public CuboidWrapper inset(CuboidDirection dir, int amount) {
        return this.outset(dir, -amount);
    }

    /**
     * Return true if the point at (x,y,z) is contained within this CuboidWrapper.
     *
     * @param x - The X co-ordinate
     * @param y - The Y co-ordinate
     * @param z - The Z co-ordinate
     * @return true if the given point is within this CuboidWrapper, false otherwise
     */
    public boolean contains(int x, int y, int z) {
        return x >= this.x1
                && x <= this.x2
                && y >= this.y1
                && y <= this.y2
                && z >= this.z1
                && z <= this.z2;
    }

    /**
     * Check if the given Block is contained within this CuboidWrapper.
     *
     * @param b - The Block to check for
     * @return true if the Block is within this CuboidWrapper, false otherwise
     */
    public boolean contains(Block b) {
        return this.contains(b.getLocation());
    }

    /**
     * Check if the given Location is contained within this CuboidWrapper.
     *
     * @param l - The Location to check for
     * @return true if the Location is within this CuboidWrapper, false otherwise
     */
    public boolean contains(Location l) {
        if (!this.worldName.equals(l.getWorld().getName())) return false;
        return this.contains(l.getBlockX(), l.getBlockY(), l.getBlockZ());
    }

    /**
     * Get the volume of this CuboidWrapper.
     *
     * @return The CuboidWrapper volume, in blocks
     */
    public int getVolume() {
        return this.getSizeX() * this.getSizeY() * this.getSizeZ();
    }

    /**
     * Get the average light level of all empty (air) blocks in the CuboidWrapper. Returns 0 if there
     * are no empty blocks.
     *
     * @return The average light level of this CuboidWrapper
     */
    public byte getAverageLightLevel() {
        long total = 0;
        int n = 0;
        for (Block b : this) {
            if (b.isEmpty()) {
                total += b.getLightLevel();
                ++n;
            }
        }
        return n > 0 ? (byte) (total / n) : 0;
    }

    /**
     * Contract the CuboidWrapper, returning a CuboidWrapper with any air around the edges removed,
     * just large enough to include all non-air blocks.
     *
     * @return A new CuboidWrapper with no external air blocks
     */
    public CuboidWrapper contract() {
        return this.contract(CuboidDirection.Down)
                .contract(CuboidDirection.South)
                .contract(CuboidDirection.East)
                .contract(CuboidDirection.Up)
                .contract(CuboidDirection.North)
                .contract(CuboidDirection.West);
    }

    /**
     * Contract the CuboidWrapper in the given direction, returning a new CuboidWrapper which has no
     * exterior empty space. E.g. A direction of Down will push the top face downwards as much as
     * possible.
     *
     * @param dir - The direction in which to contract
     * @return A new CuboidWrapper contracted in the given direction
     */
    public CuboidWrapper contract(CuboidDirection dir) {
        CuboidWrapper face = getFace(dir.opposite());
        switch (dir) {
            case Down:
                while (face.containsOnly(Material.AIR) && face.getLowerY() > this.getLowerY()) {
                    face = face.shift(CuboidDirection.Down, 1);
                }
                return new CuboidWrapper(
                        this.worldName, this.x1, this.y1, this.z1, this.x2, face.getUpperY(), this.z2);
            case Up:
                while (face.containsOnly(Material.AIR) && face.getUpperY() < this.getUpperY()) {
                    face = face.shift(CuboidDirection.Up, 1);
                }
                return new CuboidWrapper(
                        this.worldName, this.x1, face.getLowerY(), this.z1, this.x2, this.y2, this.z2);
            case North:
                while (face.containsOnly(Material.AIR) && face.getLowerX() > this.getLowerX()) {
                    face = face.shift(CuboidDirection.North, 1);
                }
                return new CuboidWrapper(
                        this.worldName, this.x1, this.y1, this.z1, face.getUpperX(), this.y2, this.z2);
            case South:
                while (face.containsOnly(Material.AIR) && face.getUpperX() < this.getUpperX()) {
                    face = face.shift(CuboidDirection.South, 1);
                }
                return new CuboidWrapper(
                        this.worldName, face.getLowerX(), this.y1, this.z1, this.x2, this.y2, this.z2);
            case East:
                while (face.containsOnly(Material.AIR) && face.getLowerZ() > this.getLowerZ()) {
                    face = face.shift(CuboidDirection.East, 1);
                }
                return new CuboidWrapper(
                        this.worldName, this.x1, this.y1, this.z1, this.x2, this.y2, face.getUpperZ());
            case West:
                while (face.containsOnly(Material.AIR) && face.getUpperZ() < this.getUpperZ()) {
                    face = face.shift(CuboidDirection.West, 1);
                }
                return new CuboidWrapper(
                        this.worldName, this.x1, this.y1, face.getLowerZ(), this.x2, this.y2, this.z2);
            default:
                throw new IllegalArgumentException("Invalid direction " + dir);
        }
    }

    /**
     * Get the CuboidWrapper representing the face of this CuboidWrapper. The resulting CuboidWrapper
     * will be one block thick in the axis perpendicular to the requested face.
     *
     * @param dir - which face of the CuboidWrapper to get
     * @return The CuboidWrapper representing this CuboidWrapper's requested face
     */
    public CuboidWrapper getFace(CuboidDirection dir) {
        switch (dir) {
            case Down:
                return new CuboidWrapper(
                        this.worldName, this.x1, this.y1, this.z1, this.x2, this.y1, this.z2);
            case Up:
                return new CuboidWrapper(
                        this.worldName, this.x1, this.y2, this.z1, this.x2, this.y2, this.z2);
            case North:
                return new CuboidWrapper(
                        this.worldName, this.x1, this.y1, this.z1, this.x1, this.y2, this.z2);
            case South:
                return new CuboidWrapper(
                        this.worldName, this.x2, this.y1, this.z1, this.x2, this.y2, this.z2);
            case East:
                return new CuboidWrapper(
                        this.worldName, this.x1, this.y1, this.z1, this.x2, this.y2, this.z1);
            case West:
                return new CuboidWrapper(
                        this.worldName, this.x1, this.y1, this.z2, this.x2, this.y2, this.z2);
            default:
                throw new IllegalArgumentException("Invalid direction " + dir);
        }
    }

    /**
     * Check if the CuboidWrapper contains only blocks of the given type
     *
     * @param type - The block Type to check for
     * @return true if this CuboidWrapper contains only blocks of the given type
     */
    public boolean containsOnly(Material type) {
        XMaterial material = XMaterial.matchXMaterial(type);

        for (Block b : this) {
            if (XMaterial.matchXMaterial(b.getType()) != material) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the CuboidWrapper big enough to hold both this CuboidWrapper and the given one.
     *
     * @param other - The other cuboid.
     * @return A new CuboidWrapper large enough to hold this CuboidWrapper and the given CuboidWrapper
     */
    public CuboidWrapper getBoundingCuboid(CuboidWrapper other) {
        if (other == null) return this;

        int xMin = Math.min(this.getLowerX(), other.getLowerX());
        int yMin = Math.min(this.getLowerY(), other.getLowerY());
        int zMin = Math.min(this.getLowerZ(), other.getLowerZ());
        int xMax = Math.max(this.getUpperX(), other.getUpperX());
        int yMax = Math.max(this.getUpperY(), other.getUpperY());
        int zMax = Math.max(this.getUpperZ(), other.getUpperZ());

        return new CuboidWrapper(this.worldName, xMin, yMin, zMin, xMax, yMax, zMax);
    }

    /**
     * Get a block relative to the lower NE point of the CuboidWrapper.
     *
     * @param x - The X co-ordinate
     * @param y - The Y co-ordinate
     * @param z - The Z co-ordinate
     * @return The block at the given position
     */
    public Block getRelativeBlock(int x, int y, int z) {
        return this.getWorld().getBlockAt(this.x1 + x, this.y1 + y, this.z1 + z);
    }

    /**
     * Get a block relative to the lower NE point of the CuboidWrapper in the given World. This
     * version of getRelativeBlock() should be used if being called many times, to avoid excessive
     * calls to getWorld().
     *
     * @param w - The world
     * @param x - The X co-ordinate
     * @param y - The Y co-ordinate
     * @param z - The Z co-ordinate
     * @return The block at the given position
     */
    public Block getRelativeBlock(World w, int x, int y, int z) {
        return w.getBlockAt(this.x1 + x, y1 + y, this.z1 + z);
    }

    /**
     * Get a list of the chunks which are fully or partially contained in this cuboid.
     *
     * @return A list of Chunk objects
     */
    public List<Chunk> getChunks() {
        List<Chunk> res = new ArrayList<Chunk>();

        World w = this.getWorld();
        int x1 = this.getLowerX() & ~0xf;
        int x2 = this.getUpperX() & ~0xf;
        int z1 = this.getLowerZ() & ~0xf;
        int z2 = this.getUpperZ() & ~0xf;
        for (int x = x1; x <= x2; x += 16) {
            for (int z = z1; z <= z2; z += 16) {
                res.add(w.getChunkAt(x >> 4, z >> 4));
            }
        }
        return res;
    }

    /**
     * Executes iterator.
     */
    public Iterator<Block> iterator() {
        return new CuboidIterator(
                this.getWorld(), this.x1, this.y1, this.z1, this.x2, this.y2, this.z2);
    }

    /**
     * Executes clone.
     */
    @Override
    public CuboidWrapper clone() {
        return new CuboidWrapper(this);
    }

    /**
     * Converts to string.
     */
    @Override
    public String toString() {
        return new String(
                "CuboidWrapper: "
                        + this.worldName
                        + ","
                        + this.x1
                        + ","
                        + this.y1
                        + ","
                        + this.z1
                        + "=>"
                        + this.x2
                        + ","
                        + this.y2
                        + ","
                        + this.z2);
    }

    /**
     * Represents the cuboid iterator component.
     */
    public class CuboidIterator implements Iterator<Block> {
        /**
         * Stores w.
         */
        private World w;

        /**
         * Stores this field value.
         */
        private int baseX, baseY, baseZ;

        /**
         * Stores this field value.
         */
        private int x, y, z;

        /**
         * Stores this field value.
         */
        private int sizeX, sizeY, sizeZ;

        /**
         * Creates a new CuboidIterator instance.
         */
        public CuboidIterator(World w, int x1, int y1, int z1, int x2, int y2, int z2) {
            this.w = w;
            this.baseX = x1;
            this.baseY = y1;
            this.baseZ = z1;
            this.sizeX = Math.abs(x2 - x1) + 1;
            this.sizeY = Math.abs(y2 - y1) + 1;
            this.sizeZ = Math.abs(z2 - z1) + 1;
            this.x = this.y = this.z = 0;
        }

        /**
         * Checks whether next.
         */
        public boolean hasNext() {
            return this.x < this.sizeX && this.y < this.sizeY && this.z < this.sizeZ;
        }

        /**
         * Executes next.
         */
        public Block next() {
            Block b = this.w.getBlockAt(this.baseX + this.x, this.baseY + this.y, this.baseZ + this.z);
            if (++x >= this.sizeX) {
                this.x = 0;
                if (++this.y >= this.sizeY) {
                    this.y = 0;
                    ++this.z;
                }
            }
            return b;
        }

        /**
         * Executes remove.
         */
        public void remove() {
        }
    }

    /**
     * Enumerates cuboid direction values.
     */
    public enum CuboidDirection {
        North,
        East,
        South,
        West,
        Up,
        Down,
        Horizontal,
        Vertical,
        Both,
        Unknown;

        /**
         * Executes opposite.
         */
        public CuboidDirection opposite() {
            switch (this) {
                case North:
                    return South;
                case East:
                    return West;
                case South:
                    return North;
                case West:
                    return East;
                case Horizontal:
                    return Vertical;
                case Vertical:
                    return Horizontal;
                case Up:
                    return Down;
                case Down:
                    return Up;
                case Both:
                    return Both;
                default:
                    return Unknown;
            }
        }
    }

    /**
     * Executes overlaps.
     */
    public boolean overlaps(CuboidWrapper other) {
        if (other.getWorld() != this.getWorld()) {
            return false;
        }

        int minX1 = this.getLowerX();
        int minY1 = this.getLowerY();
        int minZ1 = this.getLowerZ();
        int maxX1 = this.getUpperX();
        int maxY1 = this.getUpperY();
        int maxZ1 = this.getUpperZ();

        int minX2 = other.getLowerX();
        int minY2 = other.getLowerY();
        int minZ2 = other.getLowerZ();
        int maxX2 = other.getUpperX();
        int maxY2 = other.getUpperY();
        int maxZ2 = other.getUpperZ();

        // Check for non-overlapping conditions
        if (maxX1 < minX2 || minX1 > maxX2) {
            return false;
        }
        if (maxY1 < minY2 || minY1 > maxY2) {
            return false;
        }
        if (maxZ1 < minZ2 || minZ1 > maxZ2) {
            return false;
        }

        // Iterate through the blocks of the current CuboidWrapper
        for (Block block : this) {
            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();

            // Check if the block is within the other CuboidWrapper
            if (x >= minX2 && x <= maxX2 && y >= minY2 && y <= maxY2 && z >= minZ2 && z <= maxZ2) {
                return true; // Overlapping block found
            }
        }

        return false; // No overlapping blocks found
    }

    /**
     * Gets width.
     */
    public int getWidth() {
        return this.getUpperX() - this.getLowerX();
    }

    /**
     * Gets length.
     */
    public int getLength() {
        return this.getUpperZ() - this.getLowerZ();
    }

    /**
     * Gets location1.
     */
    public Location getLocation1() {
        return location1;
    }

    /**
     * Gets location2.
     */
    public Location getLocation2() {
        return location2;
    }

    /**
     * Gets walls.
     */
    public List<Block> getWalls(int min, int max) {
        List<Block> blocks = new ArrayList<>();
        World world = this.getWorld();

        for (int y = min; y <= max; ++y) {
            for (int x = this.x1; x <= this.x2; x++) {
                blocks.add(world.getBlockAt(x, y, this.z1));
                blocks.add(world.getBlockAt(x, y, this.z2));
            }

            for (int z = this.z1; z <= this.z2; z++) {
                blocks.add(world.getBlockAt(this.x1, y, z));
                blocks.add(world.getBlockAt(this.x2, y, z));
            }
        }
        return blocks;
    }
}
