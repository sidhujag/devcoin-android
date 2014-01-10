/**
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.bitcoin.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Date;

/**
 * <p>A block is a group of transactions, and is one of the fundamental data structures of the Bitcoin system.
 * It records a set of {@link com.google.bitcoin.core.Transaction}s together with some data that links it into a place in the global block
 * chain, and proves that a difficult calculation was done over its contents. See
 * <a href="http://www.bitcoin.org/bitcoin.pdf">the Bitcoin technical paper</a> for
 * more detail on blocks. <p/>
 *
 * To get a block, you can either build one from the raw bytes you can get from another implementation, or request one
 * specifically using {@link com.google.bitcoin.core.Peer#getBlock(com.google.bitcoin.core.Sha256Hash)}, or grab one from a downloaded {@link com.google.bitcoin.core.BlockChain}.
 */
public class BlockMergeMined {
    private static final Logger log = LoggerFactory.getLogger(BlockMergeMined.class);
    private static final long serialVersionUID = 2738858929966035281L;
    public static final long BLOCK_VERSION_DEFAULT = 1;
    public static final long DEVCOIN_MERGED_MINE_START_TIME = 1325974424L;
    public static final long BLOCK_VERSION_AUXPOW = (1 << 8);
    public static final long BLOCK_VERSION_CHAIN_START = (1 << 16);
    public static final long BLOCK_VERSION_CHAIN_END = (1 << 30);
    public static final long DEVCOIN_MERGED_MINE_CHAIN_ID = 0x0004;
    public static final byte pchMergedMiningHeader[] = { (byte)0xfa, (byte)0xbe, 'm', 'm' } ;
    private transient boolean headerParsed;
    // Fields defined as part of the protocol format.
    // modifiers

    private Sha256Hash prevBlockHash;
    private Sha256Hash merkleRoot;
    private NetworkParameters params;
    public Block block;
    public BlockMergeMinedPayload payload;
    private int cursor;
    /** Stores the hash of the block. If null, getHash() will recalculate it. */
    private transient Sha256Hash hash;

    BlockMergeMined(NetworkParameters netparams)
    {
        payload = null;
        params = netparams;
        cursor = 0;
        headerParsed = false;
        setBlock(null);
    }
    /** Constructs a block object from the Bitcoin wire format. */
    public BlockMergeMined(NetworkParameters netparams, byte[] payloadBytes, int cursor, Block block) throws ProtocolException {
        headerParsed = false;
        this.cursor = cursor;
        params = netparams;
        payload = new BlockMergeMinedPayload(this.params, payloadBytes, cursor, block);
        setBlock(block);
        headerParsed = true;
    }
    /** Constructs a block object from the Bitcoin wire format. */
    public BlockMergeMined(NetworkParameters netparams,BlockMergeMinedPayload payload, int cursor, Block block) throws ProtocolException {
        headerParsed = false;
        this.cursor = cursor;
        params = netparams;
        this.payload = payload.cloneAsHeader();
        setBlock(block);
        headerParsed = true;
    }
    private void setBlock(Block block)
    {
        this.block = block;
        if(this.payload != null && this.payload.block == null)
            this.payload.block = block;
    }
    public long GetChainID(long ver)
    {
        return ver / BLOCK_VERSION_CHAIN_START;
    }
    public long GetChainID()
    {
        return block.getVersion() / BLOCK_VERSION_CHAIN_START;
    }
    public boolean IsValid()
    {
        return payload != null && payload.IsValid() && headerParsed;
    }
    private void parseHeader() throws ProtocolException {
        headerParsed = false;
        payload.parse();
        headerParsed = true;
    }

    protected void parse() throws ProtocolException {

        parseHeader();

    }
    public void maybeParse()
    {
        maybeParseHeader();
    }
    /*
     * Block uses some special handling for lazy parsing and retention of cached bytes. Parsing and serializing the
     * block header and the transaction list are both non-trivial so there are good efficiency gains to be had by
     * separating them. There are many cases where a user may need to access or change one or the other but not both.
     *
     * With this in mind we ignore the inherited checkParse() and unCache() methods and implement a separate version
     * of them for both header and transactions.
     *
     * Serializing methods are also handled in their own way. Whilst they deal with separate parts of the block structure
     * there are some interdependencies. For example altering a tx requires invalidating the Merkle root and therefore
     * the cached header bytes.
     */

    private void maybeParseHeader() {
        if (IsValid())
            return;
        try {
            parseHeader();
            if (!(IsValid()))
            {
                if(payload!= null)
                    payload = null;
            }
        } catch (ProtocolException e) {
            log.info("Warning: BlockMergeMined could not parse header information!");
        }
    }
    // default for testing

    protected void writeHeader(OutputStream stream) throws IOException {

    }
    /**
     * Special handling to check if we have a valid byte array for both header
     * and transactions
     *
     * @throws java.io.IOException
     */

    public byte[] bitcoinSerialize() {


        return null;
    }

    public long getParentBlockDifficulty()
    {
        return payload.parentBlockHeader.getDifficultyTarget();
    }
    public Sha256Hash getParentBlockHash()
    {
        return payload.hashOfParentBlockHeader;
    }
    /** Returns the version of the block data structure as defined by the Bitcoin protocol. */
    public long getVersion() {
        return block.getVersion();
    }

    /**
     * Returns the nonce, an arbitrary value that exists only to make the hash of the block header fall below the
     * difficulty target.
     */
    public long getNonce() {
        return block.getNonce();
    }


    /** Returns a copy of the block, but without any transactions. */
    public BlockMergeMined cloneAsHeader()  {
        maybeParseHeader();
        try
        {
            BlockMergeMined block = new BlockMergeMined(this.params, this.payload, this.cursor, this.block);

            return block;
        }
        catch(ProtocolException e)
        {
            return null;
        }
    }

    /**
     * Returns a multi-line string containing a description of the contents of
     * the block. Use for debugging purposes only.
     */

    public String toString() {
        StringBuilder s = new StringBuilder("");
        s.append("      version: v");
        s.append(block.getVersion());
        s.append("\n");
        s.append("      time: [");
        s.append(block.getTime());
        s.append("] ");
        s.append(new Date(block.getTimeSeconds() * 1000));
        s.append("\n");
        s.append("      difficulty target (nBits): ");
        s.append(block.getDifficultyTarget());
        s.append("\n");
        s.append("      nonce: ");
        s.append(block.getNonce());
        s.append("\n");
        if(hash != null)
        {
            s.append("      hash: ");
            s.append(block.getHashAsString());
            s.append("\n");
        }
        if(payload != null)
        {
            s.append("\n");
            s.append(payload.toString());
        }
        return s.toString();
    }


    static int matchData(byte[] srcData, byte[] dataToFind) {
        int iDataLen = srcData.length;
        int iDataToFindLen = dataToFind.length;
        boolean bGotData = false;
        int iMatchDataCntr = 0;
        for (int i = 0; i < iDataLen; i++) {
            if (srcData[i] == dataToFind[iMatchDataCntr]) {
                iMatchDataCntr++;
                bGotData = true;
            } else {
                if (srcData[i] == dataToFind[0]) {
                    iMatchDataCntr = 1;
                } else {
                    iMatchDataCntr = 0;
                    bGotData = false;
                }
            }
            if (iMatchDataCntr == iDataToFindLen) {
                return i;
            }
        }

        return -1;
    }
    public int getCursor()
    {
        if(payload != null)
            return payload.cursor;
        else
            return 0;
    }
    public int getMessageSize()
    {
        if(payload != null)
            return payload.length;
        else
            return 0;
    }
    protected BigInteger getDifficultyTargetAsInteger(long targetlong) throws VerificationException {
        maybeParseHeader();
        BigInteger target = Utils.decodeCompactBits(targetlong);
        if (target.compareTo(BigInteger.ZERO) <= 0 || target.compareTo(params.proofOfWorkLimit) > 0)
            throw new VerificationException("Difficulty target is bad: " + target.toString());
        return target;
    }
    /** Returns true if the hash of the block is OK (lower than difficulty target). */

    protected boolean checkProofOfWork(boolean throwException) throws VerificationException {
        if(GetChainID() != DEVCOIN_MERGED_MINE_CHAIN_ID)
        {
            throw new VerificationException("Merged-mine block does not have the correct chain ID required for Devcoin blocks, Current ID: " + GetChainID() + " Expected: " + DEVCOIN_MERGED_MINE_CHAIN_ID);
        }
        if(GetChainID(payload.parentBlockHeader.getVersion()) == DEVCOIN_MERGED_MINE_CHAIN_ID)
        {
            throw new VerificationException("Merged-mine block Aux POW parent has our chain ID: " + DEVCOIN_MERGED_MINE_CHAIN_ID);
        }
        int headerIndex = matchData(payload.parentBlockCoinBaseTx.getInput(0).getScriptBytes(), this.pchMergedMiningHeader);
        if(headerIndex > 0)
        {
            if(!payload.parentBlockCoinBaseTx.getInput(0).isCoinBase())
            {
                throw new VerificationException("Parent coinbase transaction not an actual coinbase transaction!");
            }
        }
        else
        {
            log.info("Warning: Merged-mine header not found in parent coinbase transaction script!");
        }
        return true;
    }

    /**
     * Checks the block data to ensure it follows the rules laid out in the network parameters. Specifically,
     * throws an exception if the proof of work is invalid, or if the timestamp is too far from what it should be.
     * This is <b>not</b> everything that is required for a block to be valid, only what is checkable independent
     * of the chain and without a transaction index.
     *
     * @throws com.google.bitcoin.core.VerificationException
     */
    public void verifyHeader() throws VerificationException {
        // Prove that this block is OK. It might seem that we can just ignore most of these checks given that the
        // network is also verifying the blocks, but we cannot as it'd open us to a variety of obscure attacks.
        //
        // Firstly we need to ensure this block does in fact represent real work done. If the difficulty is high
        // enough, it's probably been done by the network.
        maybeParseHeader();
        checkProofOfWork(true);
    }




    /**
     * Verifies both the header and that the transactions hash to the merkle root.
     */
    public void verify() throws VerificationException {
        verifyHeader();
    }


    public boolean equals(Object o) {
        if (!(o instanceof BlockMergeMined))
            return false;
        BlockMergeMined other = (BlockMergeMined) o;
        return block.getTimeSeconds() == other.block.getTimeSeconds();
    }

    /**
     * Returns the time at which the block was solved and broadcast, according to the clock of the solving node. This
     * is measured in seconds since the UNIX epoch (midnight Jan 1st 1970).
     */
    public long getTimeSeconds() {
        return block.getTimeSeconds();
    }

    /**
     * Returns the time at which the block was solved and broadcast, according to the clock of the solving node.
     */
    public Date getTime() {
        return new Date(getTimeSeconds()*1000);
    }
    /**
     * Returns the difficulty of the proof of work that this block should meet encoded <b>in compact form</b>. The {@link
     * com.google.bitcoin.core.BlockChain} verifies that this is not too easy by looking at the length of the chain when the block is added.
     *
     */
    public long getDifficultyTarget() {
        return block.getDifficultyTarget();
    }


}
