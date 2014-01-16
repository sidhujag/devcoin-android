package com.google.devcoin.tools;

import java.io.File;

import com.google.devcoin.core.AbstractBlockChain;
import com.google.devcoin.core.Block;
import com.google.devcoin.core.BlockChain;
import com.google.devcoin.core.FullPrunedBlockChain;
import com.google.devcoin.core.NetworkParameters;
import com.google.devcoin.core.PrunedException;
import com.google.devcoin.core.VerificationException;
import com.google.devcoin.params.MainNetParams;
import com.google.devcoin.params.TestNet3Params;
import com.google.devcoin.store.BlockStore;
import com.google.devcoin.store.BlockStoreException;
import com.google.devcoin.store.BoundedOverheadBlockStore;
import com.google.devcoin.store.FullPrunedBlockStore;
import com.google.devcoin.store.H2FullPrunedBlockStore;
import com.google.devcoin.store.MemoryBlockStore;
import com.google.devcoin.store.MemoryFullPrunedBlockStore;
import com.google.devcoin.store.SPVBlockStore;
import com.google.devcoin.utils.BlockFileLoader;
import com.google.common.base.Preconditions;

/** Very thin wrapper around {@link com.google.devcoin.util.BlockFileLoader} */
public class BlockImporter {
    public static void main(String[] args) throws BlockStoreException, VerificationException, PrunedException {
        System.out.println("USAGE: BlockImporter (prod|test) (H2|BoundedOverhead|Disk|MemFull|Mem|SPV) [blockStore]");
        System.out.println("       blockStore is required unless type is Mem or MemFull");
        System.out.println("       eg BlockImporter prod H2 /home/user/bitcoinj.h2store");
        System.out.println("       Does full verification if the store supports it");
        Preconditions.checkArgument(args.length == 2 || args.length == 3);
        
        NetworkParameters params = null;
        if (args[0].equals("test"))
            params = TestNet3Params.get();
        else
            params = MainNetParams.get();
        
        BlockStore store = null;
        if (args[1].equals("H2")) {
            Preconditions.checkArgument(args.length == 3);
            store = new H2FullPrunedBlockStore(params, args[2], 100);
        } else if (args[1].equals("BoundedOverhead")) {
            Preconditions.checkArgument(args.length == 3);
            store = new BoundedOverheadBlockStore(params, new File(args[2]));
        } else if (args[1].equals("MemFull")) {
            Preconditions.checkArgument(args.length == 2);
            store = new MemoryFullPrunedBlockStore(params, 100);
        } else if (args[1].equals("Mem")) {
            Preconditions.checkArgument(args.length == 2);
            store = new MemoryBlockStore(params);
        } else if (args[1].equals("SPV")) {
            Preconditions.checkArgument(args.length == 3);
            store = new SPVBlockStore(params, new File(args[2]));
        }
        
        AbstractBlockChain chain = null;
        if (store instanceof FullPrunedBlockStore)
            chain = new FullPrunedBlockChain(params, (FullPrunedBlockStore) store);
        else
            chain = new BlockChain(params, store);
        
        BlockFileLoader loader = new BlockFileLoader(params, BlockFileLoader.getReferenceClientBlockFileList());
        
        for (Block block : loader)
            chain.add(block);
    }
}
