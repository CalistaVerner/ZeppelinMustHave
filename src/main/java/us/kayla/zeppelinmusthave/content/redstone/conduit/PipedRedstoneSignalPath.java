package us.kayla.zeppelinmusthave.content.redstone.conduit;

record PipedRedstoneSignalPath(int power, int distance) {
    static final PipedRedstoneSignalPath OFF = new PipedRedstoneSignalPath(0, Integer.MAX_VALUE);
}
