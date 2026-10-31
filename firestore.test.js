const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read user profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("Cross-user isolation: Alice cannot read Bob's profile", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      displayName: "Bob",
      coinBalance: 100,
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
});

test("Owner can create and read their own user profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const profileRef = aliceDb.collection("users").doc(ALICE_UID);

  await assertSucceeds(
    profileRef.set({
      userId: ALICE_UID,
      displayName: "Alice",
      coinBalance: 50,
      activeThemeId: "midnight_oled",
      activeLanguage: "en",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  await assertSucceeds(profileRef.get());
});

test("Attacker cannot impersonate or write extra fields to user profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobRef = aliceDb.collection("users").doc(BOB_UID);

  // Trying to write Bob's document as Alice
  await assertFails(
    bobRef.set({
      userId: BOB_UID,
      displayName: "Bob",
      coinBalance: 100,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  // Trying to write ghost field into Alice document
  const aliceRef = aliceDb.collection("users").doc(ALICE_UID);
  await assertFails(
    aliceRef.set({
      userId: ALICE_UID,
      displayName: "Alice",
      coinBalance: 100,
      createdAt: new Date(),
      updatedAt: new Date(),
      isAdmin: true, // Ghost field
    })
  );
});

test("Redemptions: Owner can create and list redemptions; Bob cannot read them", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const redemptionRef = aliceDb.collection("users").doc(ALICE_UID).collection("redemptions").doc("redemp_1");

  await assertSucceeds(
    redemptionRef.set({
      id: "redemp_1",
      userId: ALICE_UID,
      cardTitle: "Google Play Gift Card",
      cardValue: "$5.00",
      code: "PLAY-ABCD-1234-XYZ9",
      cost: 500,
      createdAt: new Date(),
    })
  );

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("redemptions").get());

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("redemptions").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("redemptions").doc("redemp_1").get());
});

test("Redemptions: Cannot be updated or deleted (immutable ledger)", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const redemptionRef = aliceDb.collection("users").doc(ALICE_UID).collection("redemptions").doc("redemp_1");

  await assertSucceeds(
    redemptionRef.set({
      id: "redemp_1",
      userId: ALICE_UID,
      cardTitle: "Google Play Gift Card",
      cardValue: "$5.00",
      code: "PLAY-ABCD-1234-XYZ9",
      cost: 500,
      createdAt: new Date(),
    })
  );

  await assertFails(redemptionRef.update({ cost: 0 }));
  await assertFails(redemptionRef.delete());
});

test("Custom themes: Owner can create, list, and delete custom themes", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const themeRef = aliceDb.collection("users").doc(ALICE_UID).collection("custom_themes").doc("custom_1");

  await assertSucceeds(
    themeRef.set({
      id: "custom_1",
      userId: ALICE_UID,
      name: "Neon Cyber",
      backgroundColorHex: "#0D0E15",
      keyColorHex: "#1E2235",
      textColorHex: "#00E5FF",
      accentColorHex: "#FF007F",
      createdAt: new Date(),
    })
  );

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("custom_themes").get());
  await assertSucceeds(themeRef.delete());
});
