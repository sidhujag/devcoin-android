package com.google.bitcoin.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jagdeep.sidhu on 1/5/14.
 */
public class BlockMergeMinedPayload  {
    private static final Logger log = LoggerFactory.getLogger(BlockMergeMinedPayload.class);
    public byte[] bytes;
    public int cursor;
    NetworkParameters params;
    // Merged mining fields
    //Parent Block TX
    public Transaction parentBlockCoinBaseTx;
    public Block block;
    int length = 0;
    //Coinbase Link
    public Sha256Hash hashOfParentBlockHeader;

    //Parent Block Header
    public Block parentBlockHeader;
    public BlockMergeMinedPayload(NetworkParameters parameters, byte[] bytePayload, int cursorStart, Block block) throws ProtocolException
    {
        this.block = block;
        this.params = parameters;
        bytes = bytePayload;
        cursor = cursorStart;

    }

    void parse() throws ProtocolException
    {
        int cursorStart = cursor;
        parseMergedMineInfo();
        length = cursor-cursorStart;

    }
    private void parseMergedMineInfo() throws ProtocolException
    {
        // Parent Block Coinbase Transaction:
        // Version

        parentBlockCoinBaseTx = new Transaction(params, bytes, cursor, this.block, false, true, Block.UNKNOWN_LENGTH);
        parentBlockCoinBaseTx.getConfidence().setSource(TransactionConfidence.Source.NETWORK);
        cursor += parentBlockCoinBaseTx.getMessageSize();
        // Coinbase Link:
        // Hash of parent block header
        hashOfParentBlockHeader = readHash();

        // Number of links in branch

        long numHashes = readVarInt();

        // Hash #1 - #numHashes
        cursor += 32*numHashes;

        // Branch sides bitmask

        cursor += 4;

        // Aux Blockchain Link:
        // Number of links in branch
        numHashes = readVarInt();
        // Hash #1 - #numHashes
        cursor += 32*numHashes;

        // Branch sides bitmask
        cursor += 4;

        // Parent Block Header:
        parentBlockHeader = new Block(this.params, bytes, true, true, 80, cursor);
        cursor += parentBlockHeader.getMessageSize();
        Sha256Hash hashOfParentBlockHeaderCalculated =   parentBlockHeader.getHash();

        /*Note that the block_hash element is not needed as you have the full parent_block header element and can calculate the hash from that. The current Namecoin client doesn't check this field for validity, and as such some AuxPOW blocks have it little-endian, and some have it big-endian. */
        /*https://en.bitcoin.it/wiki/Merged_mining_specification*/
        if(!hashOfParentBlockHeader.equals(hashOfParentBlockHeaderCalculated))
        {
            Sha256Hash reversedHashOfParentBlockHeader =  new Sha256Hash(Utils.reverseBytes(hashOfParentBlockHeader.getBytes()));
            if(!reversedHashOfParentBlockHeader.equals(hashOfParentBlockHeaderCalculated)){
                throw new ProtocolException("Hash of parent block header calculated does not match hash of parent block header received in merged-mining header.");
            }
            else
            {
                hashOfParentBlockHeader = reversedHashOfParentBlockHeader;
            }
        }
    }

    /**
     * Returns a multi-line string containing a description of the contents of
     * the block. Use for debugging purposes only.
     */
    long readVarInt() throws ProtocolException {
        return readVarInt(0);
    }
    long readVarInt(int offset) throws ProtocolException {
        try {
            VarInt varint = new VarInt(bytes, cursor + offset);
            cursor += offset + varint.getOriginalSizeInBytes();
            return varint.value;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ProtocolException(e);
        }
    }
    public String toString() {
        StringBuilder s = new StringBuilder("");
        s.append(" parent block coin base transaction: \n");
        s.append(parentBlockCoinBaseTx.toString());
        s.append("\n");
        if(hashOfParentBlockHeader != null)
        {
            s.append(" coinbase link: \n");
            s.append("   hash of parent block header: ");
            s.append(hashOfParentBlockHeader);
            s.append("\n");
        }
        if(parentBlockHeader != null)
        {
            s.append(" parent block header: \n");
            s.append(parentBlockHeader.toString());
            s.append("\n");
        }
        return s.toString();
    }
    byte[] readBytes(int length) throws ProtocolException {
        try {
            byte[] b = new byte[length];
            System.arraycopy(bytes, cursor, b, 0, length);
            cursor += length;
            return b;
        } catch (IndexOutOfBoundsException e) {
            throw new ProtocolException(e);
        }
    }
    long readInt64() throws ProtocolException {
        try {
            long u = Utils.readInt64(bytes, cursor);
            cursor += 8;
            return u;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ProtocolException(e);
        }
    }
    long readUint32() throws ProtocolException {
        try {
            long u = Utils.readUint32(bytes, cursor);
            cursor += 4;
            return u;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ProtocolException(e);
        }
    }

    Sha256Hash readHash() throws ProtocolException {
        try {
            byte[] hash = new byte[32];
            System.arraycopy(bytes, cursor, hash, 0, 32);
            // We have to flip it around, as it's been read off the wire in little endian.
            // Not the most efficient way to do this but the clearest.
            hash = Utils.reverseBytes(hash);
            cursor += 32;
            return new Sha256Hash(hash);
        } catch (IndexOutOfBoundsException e) {
            throw new ProtocolException(e);
        }
    }
}
