import { LinearProgress } from "@mui/material";
import { request } from "api";
import { useEffect, useState } from "react";
import AccessDenied from "./AccessDenied";

export function withAuthorization(Component, id) {
  console.log("id ", id);
  return function ScreenSecurityComponent({ ...props }) {
    const [checking, setChecking] = useState(true);
    const [screenAuthorization, setScreenAuthorization] = useState(null); // null rõ ràng hơn undefined

    useEffect(() => {
      const fetchAuthorization = async () => {
        try {
          const res = await request("get", `/entity-authorization/${id}`);
          setScreenAuthorization(new Set(res.data));
        } catch (e) {
          console.error("Authorization error:", e);
          setScreenAuthorization(new Set()); // fallback to empty set
        } finally {
          setChecking(false);
        }
      };

      fetchAuthorization();
    }, [id]);

    if (checking) {
      return (
        <LinearProgress
          style={{
            position: "absolute",
            top: 0,
            left: -1,
            width: "100%",
            zIndex: 1202,
          }}
        />
      );
    }

    if (screenAuthorization?.has(`${id}`)) {
      return (
        <Component
          {...props}
          screenAuthorization={screenAuthorization}
        />
      );
    }

    return <AccessDenied />;
  };
}

