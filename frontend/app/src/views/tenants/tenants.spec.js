import { describeFeature, loadFeature } from "@amiceli/vitest-cucumber";
import { mount, flushPromises } from "@vue/test-utils";
import TenantsView from "./index.js";
import { useAuthStore } from "../../stores/auth";
import { useTenantStore } from "../../stores/tenant";
import * as platformApi from "../../services/api/platform";
import { vi, expect } from "vitest";

const feature = await loadFeature("frontend/app/src/views/tenants/tenants.feature");

describeFeature(feature, ({ Scenario }) => {
  Scenario("Successfully register a new tenant", ({ Given, And, When, Then }) => {
    let wrapper;
    let authStore;
    let tenantStore;

    Given("I am logged in as a platform administrator", () => {
      authStore = useAuthStore();
      authStore.token = "admin-token";
      authStore.user = {
        id: "admin-1",
        permissions: ["platform.tenants.write", "platform.tenants.read"]
      };

      vi.spyOn(platformApi, "fetchTenants").mockResolvedValue([]);
      vi.spyOn(platformApi, "createTenant").mockImplementation((data) => {
        return Promise.resolve({
          id: "new-id",
          key: data.key,
          displayName: data.displayName,
          status: data.status
        });
      });
    });

    And("I am on the tenant management page", async () => {
      wrapper = mount(TenantsView);
      await flushPromises();
    });

    When("I enter {string} as the name and {string} as the key", async (name, key) => {
      const nameInput = wrapper.find('input[placeholder*="Name"], [id*="displayName"]');
      const keyInput = wrapper.find('input[placeholder*="Key"], [id*="key"]');
      
      // Since it's reactive form, we can also set it directly if needed, 
      // but simulating UI interaction is better for BDD
      const vm = wrapper.vm;
      vm.form.displayName = name;
      vm.form.key = key;
    });

    And("I submit the registration form", async () => {
      // Mock refresh after create
      vi.spyOn(platformApi, "fetchTenants").mockResolvedValue([
        { id: "new-id", key: "gold", displayName: "Pfandhaus Gold", status: "ACTIVE" }
      ]);
      
      await wrapper.vm.submit();
      await flushPromises();
    });

    Then("the tenant {string} should be visible in the list", (name) => {
      expect(wrapper.text()).toContain(name);
    });

    And("I should see a success message {string}", (message) => {
      expect(wrapper.vm.successMessage).toContain(message);
    });
  });
});
