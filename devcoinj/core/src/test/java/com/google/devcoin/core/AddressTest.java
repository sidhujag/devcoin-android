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

package com.google.devcoin.core;

import com.google.devcoin.params.MainNetParams;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class AddressTest {

    static final NetworkParameters mainParams = MainNetParams.get();

    @Test
    public void stringification() throws Exception {

        Address b = new Address(mainParams, Hex.decode("228a67e472ec48e31805c0bd14e8cfd204576776"));
        assertEquals("149dn8R979naoZTLVqCsmTtx7uaBpe56dB", b.toString());
    }
    
    @Test
    public void decoding() throws Exception {

        Address b = new Address(mainParams, "149dn8R979naoZTLVqCsmTtx7uaBpe56dB");
        assertEquals("228a67e472ec48e31805c0bd14e8cfd204576776", Utils.bytesToHexString(b.getHash160()));
    }
    
    @Test
    public void errorPaths() {
        // Check what happens if we try and decode garbage.
        try {
            new Address(mainParams, "this is not a valid address!");
            fail();
        } catch (WrongNetworkException e) {
            fail();
        } catch (AddressFormatException e) {
            // Success.
        }

        // Check the empty case.
        try {
            new Address(mainParams, "1C7NxZ4hbt5VHwpBQyLjmQraKHTEi1SN9i");
            //fail();
        } catch (WrongNetworkException e) {
            fail();
        } catch (AddressFormatException e) {
            // Success.
        }


    }
    
    @Test
    public void getNetwork() throws Exception {
        NetworkParameters params = Address.getParametersFromAddress("1C7NxZ4hbt5VHwpBQyLjmQraKHTEi1SN9i");
        assertEquals(MainNetParams.get().getId(), params.getId());

    }
}
