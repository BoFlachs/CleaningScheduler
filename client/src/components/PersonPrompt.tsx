import classNames from "classnames";
import { useState } from "react";
import Popup from "reactjs-popup";
import { Alert } from "./Alert";
import { isPerson, Person } from "../types";
import { addPerson } from "../services/apiPost";
import { useCleaningSchedulerContext } from "../contexts/CleaningSchedulerContext";
import { changePerson } from "../services/apiPut";

type Props = {
    isPopupVisible: boolean,
    setIsPopupVisible: () => void,
    changePrompt: boolean,
    currentPerson: Person | undefined
}

export const PersonPrompt = (props: Props) => {
    const { isPopupVisible, setIsPopupVisible, changePrompt, currentPerson } = props;
    const [alert, setAlert] = useState<string | null>(null);
    const [personName, setPersonName] = useState<string>(changePrompt ? currentPerson!.name : "");
    const [weekAvailabilityMap, setWeekAvailabilityMap] = useState<Map<number, number>>(
        !changePrompt ? new Map() :
            currentPerson!.availabilityAssignment
    );
    const { setCurrentPerson } = useCleaningSchedulerContext();
    const [weekFields, setWeekFields] = useState<{ week: string; availability: string }[]>(
        !changePrompt ? [{ week: '', availability: '' }] :
            Array.from(currentPerson!.availabilityAssignment.entries()).map(
                ([week, availability]) => ({
                    week: `${week}`,
                    availability: `${availability}`
                })
            )
    );

    const addNewFields = () => {
        setWeekFields((prevFields) => [
            ...prevFields,
            { week: '', availability: '' },
        ]);
    };

    const handleInputChange = (
        e: React.ChangeEvent<HTMLInputElement>,
        index: number,
        type: 'week' | 'availability'
    ) => {
        const newFields = [...weekFields];
        newFields[index] = { ...newFields[index], [type]: e.target.value };
        setWeekFields(newFields);

        const week = parseInt(newFields[index].week, 10);
        const availability = parseInt(newFields[index].availability, 10);
        if (!isNaN(week) && !isNaN(availability)) {
            setWeekAvailabilityMap((prevMap) => new Map(prevMap.set(week, availability)));
        }
    };

    const removeWeekField = (index: number) => {
        const newFields = [...weekFields];
        const removedField = newFields.splice(index, 1)[0];
        setWeekFields(newFields);

        const week = parseInt(removedField.week, 10);
        if (!isNaN(week)) {
            setWeekAvailabilityMap((prevMap) => {
                const newMap = new Map(prevMap);
                newMap.delete(week);
                return newMap;
            });
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        var result: Person | { statusCode: number, statusText: string }
        if (changePrompt) {
            const changedPerson: Person = {
                name: personName,
                availabilityAssignment: weekAvailabilityMap
            }
            result = await changePerson(currentPerson!.name, changedPerson)
        } else {
            const newPerson: Person = {
                name: personName,
                availabilityAssignment: weekAvailabilityMap
            }

            result = await addPerson(newPerson)
        }

        if (isPerson(result)) {
            setCurrentPerson(result)
            setIsPopupVisible()
        } else {
            setAlert(`${result.statusCode} ${result.statusText}`);
        }
    };

    return <Popup open={isPopupVisible} modal >
        {alert && <Alert text={alert} onClick={() => setAlert(null)} />}
        <div
            className={classNames(
                "fixed top-0 bottom-0 left-0 right-0",
                "bg-black/20",
                "flex"
            )}>

            <div
                className={classNames(
                    "relative",
                    "p-6 rounded-lg shadow-lg bg-[#0f2057] m-auto",
                    "w-[500px]",
                )}>
                <button
                    className={classNames(
                        "absolute top-0 right-4 ",
                        "text-4xl",
                        "text-[#4393c9] hover:text-white"
                    )}
                    onClick={() => setIsPopupVisible()}>
                    &times;
                </button>
                <h2 className="text-xl text-[#4393c9] font-semibold mb-3">
                    {changePrompt ? "Change Person" : "Add New Person"}
                </h2>
                <div className="scrollable-container">
                    <form onSubmit={handleSubmit}>
                        <div className="mb-4">
                            <label htmlFor="personName" className="mr-6 font-medium text-[#4393c9]">
                                Name:
                            </label>
                            <input
                                id="personName"
                                value={personName}
                                onChange={(e) => setPersonName(e.currentTarget.value)}
                                className="mt-1 p-2 border-[#4393c9] rounded-lg h-8 w-32 bg-[#4393c9]"
                                required />
                        </div>

                        {weekFields.map((field, index) => (
                            <div key={index} className="mb-4 grid grid-cols-3 grid-rows-2 ">
                                <label htmlFor={`week-${index}`} className="row-start-1 text-sm font-sm text-[#4393c9]">
                                    Week Number:
                                </label>
                                <input
                                    id={`week-${index}`}
                                    type="number"
                                    min="0"
                                    max="52"
                                    value={field.week}
                                    onChange={(e) => handleInputChange(e, index, 'week')}
                                    className="p-2 row-start-2 border-[#4393c9] rounded-lg h-8 w-28 bg-[#4393c9]"
                                    required
                                />
                                <label htmlFor={`availability-${index}`} className="row-start-1 text-sm font-sm text-[#4393c9]">
                                    Availability (minutes):
                                </label>
                                <input
                                    id={`availability-${index}`}
                                    type="number"
                                    min="0"
                                    value={field.availability}
                                    onChange={(e) => handleInputChange(e, index, 'availability')}
                                    className="p-2 row-start-2 border-[#4393c9] rounded-lg h-8 w-28 bg-[#4393c9]"
                                    required
                                />
                                <button
                                    type="button"
                                    onClick={() => removeWeekField(index)}
                                    className="row-start-2 col-start-3 h-8 w-20 bg-red-500 text-white rounded-lg py-1 px-2"
                                >
                                    Remove
                                </button>
                            </div>
                        ))}

                        <button
                            type="button"
                            onClick={addNewFields}
                            className="mb-8 bg-green-600 text-white py-2 px-4 rounded-lg hover:bg-green-700"
                        >
                            Add More Weeks
                        </button>

                        <div className="flex justify-center">
                            <button
                                type="submit"
                                className="w-full bg-blue-600 text-[#0f2057] py-2 px-4 rounded-lg hover:bg-blue-700"
                            >
                                {changePrompt ? "Save changes" : "Add Person"}
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </Popup>
}