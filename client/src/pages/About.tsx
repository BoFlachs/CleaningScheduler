import classNames from "classnames";
import { SideNav } from "../components/SideNav";
import "../style/about.css";

export const About = () => {
    return <>
        <aside className={classNames(
            "col-start-1",
            "bg-[#244788]",
            "flex",
            "flex-col",
            "items-center"
        )}>
            <SideNav includeSchedules={true} />
        </aside>
        <main className={classNames(
            "col-start-2 col-span-2",
            "bg-[#4393c9]",
            "flex-cols",
            "px-[20%]",
            "py-10",
            "space-y-4",
            "about",
            "text-[#0f2057]"
        )}>
            <section>
                <h2>People</h2>
                <p>In the section "Add/Edit People", you can find an overview of all people that are currently considered by the scheduler. You can also delete, add or change people from this section.
                </p>
                <h3>Overview</h3>
                <hr />
                <p>In the right side of the page you will find an overview of all people that the scheduler currently knows about. By clicking on one of the people you can find some more information. See the following section for the properties a person has.</p>

                <h3>Add a person</h3>
                <hr />
                <div className="mb-[20px]">
                    To add a person you need to provide the following information:
                    <ul className="list-disc">
                        <li>Name</li>
                        <li>Availability in minutes for each week that you want to schedule for. (If no value is given for a week, it is assumed that availability is 0 minutes.)</li>
                    </ul>
                </div>

                <h3>Change a person</h3>
                <hr />
                <p>
                    To change a person, click on the corresponding button and alter the relevant information. Remember to save the changes.
                </p>

                <h3>Delete a person</h3>
                <hr />
                <p>
                    To delete a person, click on the corresponding button. A person can only be deleted if there are no more tasks with the person as preferred assignee, and there are no more schedules that assign any tasks to this person. So first delete these tasks and schedules to allow for the deleting of this person.
                   <br /> 
                    Note that deleting is permanent and cannot be reverted.
                </p>
            </section>
            <section>
                <h2>Tasks</h2>
                <p>In the section "Add/Edit Task", you can find an overview of all tasks that are currently considered by the scheduler. You can also delete, add or change tasks from this section
                </p>
                <h3>Overview</h3>
                <hr />
                <p>In the right side of the page you will find an overview of all tasks that the scheduler currently knows about. By clicking on one of the tasks you can find some more information. See the following section for the properties a task has.</p>

                <h3>Add a task</h3>
                <hr />
                <div className="mb-[20px]">
                    To add a task you need to provide the following information:
                    <ul className="list-disc">
                        <li>Name</li>
                        <li>Costs in minutes</li>
                        <li>A preferred assignee for this task</li>
                        <li>Whether this preference should be strict. (If this option is selected, the task will also be assigned to this person if this makes the schedule sub-optimal)</li>
                        <li>What week the task was last done at. (If it has never been done before, just select a random week far outside of your intented scheduling range.)</li>
                        <li>If the task should be done every so often or just once.</li>
                        <li>If the task repeats, what the minimal amount of weeks between two repetitions should be.</li>
                        <li>If the task repeats, what the maximal amount of weeks between two repetitions should be.</li>
                    </ul>
                </div>

                <h3>Change a task</h3>
                <hr />
                <p>
                    To change a task, click on the corresponding button and alter the relevant information. Remember to save the changes.
                </p>

                <h3>Delete a task</h3>
                <hr />
                <p>
                    To delete a task, click on the corresponding button. A task can only be deleted if there are no more schedules that assign this task. So first delete these schedules to allow for the deleting of this task.
                   <br /> 
                    Note that deleting is permanent and cannot be reverted.
                </p>
            </section>
            <section>
                <h2>Schedules</h2>
                <p>In the section "Schedules", you can find all schedules that have been created until now. Furthermore, you can also create a new schedule using the people and tasks explained above.
                </p>
                <h3>Overview</h3>
                <hr />
                <p>
                    Schedules are identified by the date at which the schedule was created. Furthermore, each schedule has a score assigned to it, which can also be found in the overview. (For more on the scoring, see the next section.) Finally, you can see the actual schedule and see what task is assigned to which person each week. You can also see here how many minutes each task takes.
                </p>

                <h3>Scoring and Scheduling</h3>
                <hr />
                <p>Currently the scoring function and scheduling algorithm is a well-kept secret.</p>

                <h3>Create a new schedule</h3>
                <hr />
                <p>
                    You can create a new schedule by clicking on the "New Schedule" button. A prompt will be opened asking what week you would like the schedule to begin, and for how many weeks you would like to make a schedule. By clicking "Schedule" a new schedule will be created and shown.
                </p>

                <h3>Reschedule with locked tasks</h3>
                <hr />
                <p>Currently this feature is not yet supported.</p>

                <h3>Delete a schedule</h3>
                <hr />
                <p>
                    To delete a schedule, click on the corresponding button. 
                    <br />
                    Note that deleting is permanent and cannot be reverted.
                </p>
            </section>
        </main>
    </>
}