import React, { useEffect, useState } from "react";

const ReportPage: React.FC = () => {
  const [authenticated, setAuthenticated] = useState<boolean | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [reportUrl, setReportUrl] = useState<string | null>(null);

  console.log("BACKEND_API_URL: ");
  console.log(`${process.env.BACKEND_API_URL}`);

  useEffect(() => {
    fetch(`http://localhost:8081/check/session`, {
      credentials: "include",
    }).then((r) => {
      setAuthenticated(r.ok);
    }).catch(() => {
      setAuthenticated(false);
    });
  }, []);

  const downloadReport = async () => {
    try {
      setLoading(true);
      setError(null);
      setReportUrl(null);

      const response = await fetch(`http://localhost:8081/reports`, {
          credentials: "include",
      });

      if (!response.ok) {
        throw new Error("Failed to download report");
      }

      const data = await response.text();

      setReportUrl(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An error occurred');
    } finally {
      setLoading(false);
    }
  };

  if (authenticated === null) {
    return <div>Loading...</div>;
  }

  if (!authenticated) {
    return (
      <div className="flex flex-col items-center justify-center min-h-screen bg-gray-100">
        <button
          onClick={() =>
            (window.location.href = `http://localhost:8081/auth/login`)
          }
          className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
        >
          Login
        </button>
      </div>
    );
  }

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-gray-100">
      <div className="p-8 bg-white rounded-lg shadow-md">
        <h1 className="text-2xl font-bold mb-6">Usage Reports</h1>
        
        <button
          onClick={downloadReport}
          disabled={loading}
          className={`px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 ${
            loading ? 'opacity-50 cursor-not-allowed' : ''
          }`}
        >
          {loading ? 'Generating Report...' : 'Download Report'}
        </button>

        {reportUrl && (
          <div className="mt-4">
            <a
              href={reportUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="text-blue-600 underline"
            >
              Open report
            </a>
          </div>
        )}

        {error && (
          <div className="mt-4 p-4 bg-red-100 text-red-700 rounded">
            {error}
          </div>
        )}
      </div>
    </div>
  );
};

export default ReportPage;