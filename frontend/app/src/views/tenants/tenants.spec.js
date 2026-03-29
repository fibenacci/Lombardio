import { describeFeature, loadFeature } from "@amiceli/vitest-cucumber";
import { mount, flushPromises } from "@vue/test-utils";
import { createPinia } from "pinia";
import TenantsView from "./index.js";
import { useAuthStore } from "../../stores/auth";
import { useTenantStore } from "../../stores/tenant";
import * as platformApi from "../../services/api/platform";
import { vi, expect } from "vitest";

const feature = await loadFeature("src/views/tenants/tenants.feature");

describeFeature(feature, ({ Scenario }) => {
  Scenario("Successfully register a new tenant", ({ Given, And, When, Then }) => {
    let wrapper;
    let authStore;
    let tenantStore;
    const pinia = createPinia();

    function primePlatformAdminState() {
      authStore = useAuthStore(pinia);
      tenantStore = useTenantStore(pinia);

      authStore.token = "admin-token";
      authStore.user = {
        id: "admin-1",
        permissions: ["platform.tenants.write", "platform.tenants.read"]
      };
    }

    Given("I am logged in as a platform administrator", () => {
      primePlatformAdminState();

      vi.spyOn(platformApi, "fetchTenants").mockResolvedValue([]);
      vi.spyOn(platformApi, "fetchTenantFeatures").mockResolvedValue([]);
      vi.spyOn(platformApi, "createTenant").mockImplementation((data) => {
        return Promise.resolve({
          id: "mock-id",
          key: data.key,
          displayName: data.displayName,
          status: data.status
        });
      });
    });

    And("I am on the tenant management page", async () => {
      primePlatformAdminState();
      vi.spyOn(platformApi, "fetchTenants").mockResolvedValue([]);
      vi.spyOn(platformApi, "fetchTenantFeatures").mockResolvedValue([]);

      wrapper = mount(TenantsView, {
        global: {
          plugins: [pinia]
        }
      });
      await flushPromises();
    });

    When("I enter {string} as the name and {string} as the key", async (ctx, name, key) => {
      const vm = wrapper.vm;
      vm.form.displayName = name;
      vm.form.key = key;
    });

    And("I submit the registration form", async (ctx) => {
      primePlatformAdminState();
      vi.spyOn(platformApi, "createTenant").mockResolvedValue({
        id: "mock-id",
        key: "gold",
        displayName: "Pfandhaus Gold",
        status: "ACTIVE"
      });
      vi.spyOn(platformApi, "fetchTenants").mockResolvedValue([
        { id: "mock-id", key: "gold", displayName: "Pfandhaus Gold", status: "ACTIVE" }
      ]);
      vi.spyOn(platformApi, "fetchTenantFeatures").mockResolvedValue([]);

      await wrapper.vm.submit();
      await flushPromises();
    });

    Then("the tenant {string} should be visible in the list", async (ctx, name) => {
      await flushPromises();
      expect(tenantStore.tenants).toEqual(
        expect.arrayContaining([
          expect.objectContaining({
            displayName: name
          })
        ])
      );
      expect(wrapper.text()).toContain(name);
    });

    And("I should see a success message {string}", (ctx, message) => {
      expect(wrapper.vm.successMessage).toContain(message);
    });
  });
});
