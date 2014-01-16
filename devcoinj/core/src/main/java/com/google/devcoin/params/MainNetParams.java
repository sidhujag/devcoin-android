/*
 * Copyright 2013 Google Inc.
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

package com.google.devcoin.params;

import com.google.devcoin.core.NetworkParameters;
import com.google.devcoin.core.Sha256Hash;
import com.google.devcoin.core.Utils;

import static com.google.common.base.Preconditions.checkState;

/**
 * Parameters for the main production network on which people trade goods and services.
 */
public class MainNetParams extends NetworkParameters {
    public MainNetParams() {
        super();
        interval = INTERVAL;
        targetTimespan = TARGET_TIMESPAN;
        proofOfWorkLimit = Utils.decodeCompactBits(0x1d00ffffL);
        acceptableAddressCodes = new int[] { 0 };
        dumpedPrivateKeyHeader = 128;
        addressHeader = 0;
        port = 52333;
        packetMagic = 0x4445563aL;
        genesisBlock.setDifficultyTarget(0x1d00ffffL);
        genesisBlock.setTime(1311305081L);
        genesisBlock.setNonce(3085127155L);
        id = ID_MAINNET;
        subsidyDecreaseBlockCount = 210000;
        spendableCoinbaseDepth = 100;
        String genesisHash = genesisBlock.getHashAsString();
        checkState(genesisHash.equals("0000000062558fec003bcbf29e915cddfc34fa257dc87573f28e4520d1c7c11e"),
                genesisHash);

        // This contains (at a minimum) the blocks which are not BIP30 compliant. BIP30 changed how duplicate
        // transactions are handled. Duplicated transactions could occur in the case where a coinbase had the same
        // extraNonce and the same outputs but appeared at different heights, and greatly complicated re-org handling.
        // Having these here simplifies block connection logic considerably.f
        checkpoints.put(2500, new Sha256Hash("000000001871a2314936d39b85174cc911bf6fd58d3877412ee7b69a48e7e29e"));
        checkpoints.put(4500, new Sha256Hash("000000000967cc95711f66f804e3f431298686d681d2d5760f61856954d08faf"));
        checkpoints.put(5250, new Sha256Hash("00000000085702bfbf27daffb638be65aceb78a5f464b12539b51c1b9c548421"));
        checkpoints.put(8900, new Sha256Hash("00000000001bb8090630fcabb82ad0ab75df3eb5b008956b3ae2a352a4324f19"));
        checkpoints.put(23500, new Sha256Hash("000000000b83c3c9753d2440b91121cb0ff220bb23c136c6d09a539207e292fb"));
        checkpoints.put(54800, new Sha256Hash("04e8dcc91ff2aa0f1197f88551b4cb24ccef02ea51081b4d05ab4e3a38554137"));
        checkpoints.put(67720, new Sha256Hash("0a111b265d89f77b4c86fa6f44e3e2ad876547b1eccf19319cde922b42c1161e"));

        dnsSeeds = new String[] {
                "dvc.public.txn.co.in",
                "dvc-seed.21stcenturymoneytalk.org",
                "dvcstable01.devtome.com",
                "dvcstable01.dvcnode.org",
                "dvcstable02.dvcnode.org",
                "dvcstable03.dvcnode.org",
                "dvcstable04.dvcnode.org",
                "dvcstable05.dvcnode.org",
                "dvcstable06.dvcnode.org",
                "dvcstable07.dvcnode.org",
                "node01.dvcnode.com",
                "node02.dvcnode.com",
                "node03.dvcnode.com",
        };
    }

    private static MainNetParams instance;
    public static synchronized MainNetParams get() {
        if (instance == null) {
            instance = new MainNetParams();
        }
        return instance;
    }
}
