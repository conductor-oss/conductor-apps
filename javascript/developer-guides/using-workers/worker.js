import {
  orkesConductorClient,
  TaskManager,
} from "@io-orkes/conductor-javascript";

const worker = {
  taskDefName: "myTask",
  execute: async (task) => {
    console.log(task)
    return {
      outputData: {
        hello: "Hello " + task.inputData?.name,
      },
      status: "COMPLETED",
    };
  },
};

const config = {
  serverUrl: "https://developer.orkescloud.com/api",
  keyId: "_CHANGE_ME_",
  keySecret: "_CHANGE_ME_",
};

const client = await orkesConductorClient(config);

const manager = new TaskManager(client, [worker]);
manager.startPolling();
